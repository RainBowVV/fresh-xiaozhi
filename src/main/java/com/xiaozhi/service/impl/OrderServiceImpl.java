package com.xiaozhi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.constant.OrderConstant;
import com.xiaozhi.constant.RedisKeyConstant;
import com.xiaozhi.dto.OrderCreateDTO;
import com.xiaozhi.entity.Address;
import com.xiaozhi.entity.Order;
import com.xiaozhi.entity.OrderItem;
import com.xiaozhi.entity.Product;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.handler.AdminWebSocketHandler;
import com.xiaozhi.mapper.AddressMapper;
import com.xiaozhi.mapper.OrderItemMapper;
import com.xiaozhi.mapper.OrderMapper;
import com.xiaozhi.mapper.ProductMapper;
import com.xiaozhi.service.OrderService;
import com.xiaozhi.vo.AddressVO;
import com.xiaozhi.vo.CartItemVO;
import com.xiaozhi.vo.OrderDetailVO;
import com.xiaozhi.vo.OrderVO;
import com.xiaozhi.vo.PageResult;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AdminWebSocketHandler adminWebSocketHandler;

    private static final String CART_KEY_PREFIX = RedisKeyConstant.CART_KEY_PREFIX;

    @Override
    @Transactional
    public OrderDetailVO create(Long userId, OrderCreateDTO dto) {
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> orderedProductIds = new ArrayList<>();
        boolean shouldRemoveFromCart = false;

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            Map<Long, Integer> directItems = new java.util.LinkedHashMap<>();
            for (OrderCreateDTO.ItemDTO itemDTO : dto.getItems()) {
                if (itemDTO == null || itemDTO.getProductId() == null
                        || itemDTO.getQuantity() == null || itemDTO.getQuantity() < 1) {
                    throw new BusinessException(MessageConstant.ORDER_ITEMS_EMPTY);
                }
                directItems.merge(itemDTO.getProductId(), itemDTO.getQuantity(), Integer::sum);
            }

            // 批量查询商品
            Map<Long, Product> productMap = productMapper.selectBatchIds(directItems.keySet()).stream()
                    .filter(p -> p.getStatus() == 1)
                    .collect(Collectors.toMap(Product::getId, p -> p));

            for (Map.Entry<Long, Integer> entry : directItems.entrySet()) {
                Product product = productMap.get(entry.getKey());
                if (product == null) {
                    throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND_OR_OFFLINE);
                }
                OrderItem item = buildOrderItem(product, entry.getValue());
                orderItems.add(item);
                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        } else {
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);
            if (cartEntries.isEmpty()) {
                throw new BusinessException(MessageConstant.CART_EMPTY);
            }

            java.util.Set<Long> filterSet = null;
            if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
                filterSet = new java.util.LinkedHashSet<>(dto.getProductIds());
            }
            java.util.Set<Long> matchedIds = new java.util.LinkedHashSet<>();

            // 收集需要查询的商品ID
            List<Long> queryIds = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : cartEntries.entrySet()) {
                Long productId = Long.parseLong(entry.getKey().toString());
                if (filterSet != null && !filterSet.contains(productId)) {
                    continue;
                }
                queryIds.add(productId);
            }

            // 批量查询商品
            Map<Long, Product> productMap = productMapper.selectBatchIds(queryIds).stream()
                    .filter(p -> p.getStatus() == 1)
                    .collect(Collectors.toMap(Product::getId, p -> p));

            for (Map.Entry<Object, Object> entry : cartEntries.entrySet()) {
                Long productId = Long.parseLong(entry.getKey().toString());
                Integer quantity = Integer.parseInt(entry.getValue().toString());

                if (filterSet != null && !filterSet.contains(productId)) {
                    continue;
                }

                Product product = productMap.get(productId);
                if (product == null) {
                    throw new BusinessException(String.format(
                            MessageConstant.PRODUCT_OFFLINE_TEMPLATE, productId));
                }

                OrderItem item = buildOrderItem(product, quantity);
                orderItems.add(item);
                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                orderedProductIds.add(productId);
                matchedIds.add(productId);
            }

            if (filterSet != null && matchedIds.size() != filterSet.size()) {
                throw new BusinessException(MessageConstant.CART_PRODUCT_NOT_FOUND);
            }
            shouldRemoveFromCart = true;
        }

        if (orderItems.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_ITEMS_EMPTY);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setAddressSnapshot(JSONUtil.toJsonStr(address));
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderConstant.STATUS_PENDING_PAYMENT);
        order.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        if (shouldRemoveFromCart) {
            String cartKey = CART_KEY_PREFIX + userId;
            for (Long productId : orderedProductIds) {
                redisTemplate.opsForHash().delete(cartKey, String.valueOf(productId));
            }
        }

        long expireTime = System.currentTimeMillis() + OrderConstant.TIMEOUT_MINUTES * 60 * 1000L;
        redisTemplate.opsForZSet().add(RedisKeyConstant.ORDER_TIMEOUT_KEY, order.getId(), expireTime);

        return getDetail(userId, order.getId());
    }

    @Override
    public List<OrderVO> list(Long userId, List<Integer> statuses) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (statuses != null && !statuses.isEmpty()) {
            wrapper.in(Order::getStatus, statuses);
        }
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::toVO).toList();
    }

    @Override
    public List<OrderVO> listAll() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime));
        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getOrderNo());
            vo.setTotalAmount(o.getTotalAmount());
            vo.setStatus(o.getStatus());
            vo.setCreateTime(o.getCreateTime());
            return vo;
        }).toList();
    }

    @Override
    public PageResult<OrderVO> pageAll(Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<OrderVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private OrderVO toVO(Order o) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setStatus(o.getStatus());
        vo.setCreateTime(o.getCreateTime());
        return vo;
    }

    @Override
    public OrderDetailVO getDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return buildOrderDetailVO(order);
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus() > 1) {
            throw new BusinessException(MessageConstant.ORDER_CANNOT_CANCEL);
        }
        order.setStatus(OrderConstant.STATUS_CANCELLED);
        orderMapper.updateById(order);
        redisTemplate.opsForZSet().remove(RedisKeyConstant.ORDER_TIMEOUT_KEY, orderId);
    }

    @Override
    public void pay(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderConstant.STATUS_PENDING_PAYMENT) {
            throw new BusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderConstant.STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        redisTemplate.opsForZSet().remove(RedisKeyConstant.ORDER_TIMEOUT_KEY, orderId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "order_paid");
        notification.put("orderId", order.getId());
        notification.put("orderNo", order.getOrderNo());
        notification.put("amount", order.getTotalAmount());
        adminWebSocketHandler.sendToAll(notification);
    }

    @Override
    public OrderDetailVO getDetailById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return buildOrderDetailVO(order);
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private OrderDetailVO buildOrderDetailVO(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());

        Address address = addressMapper.selectById(order.getAddressId());
        if (address != null) {
            AddressVO addressVO = new AddressVO();
            addressVO.setId(address.getId());
            addressVO.setName(address.getName());
            addressVO.setPhone(address.getPhone());
            addressVO.setProvince(address.getProvince());
            addressVO.setCity(address.getCity());
            addressVO.setDistrict(address.getDistrict());
            addressVO.setDetail(address.getDetail());
            vo.setAddress(addressVO);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        List<CartItemVO> itemVOs = items.stream().map(item -> {
            CartItemVO itemVO = new CartItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            return itemVO;
        }).toList();
        vo.setItems(itemVOs);

        return vo;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderConstant.STATUS_COMPLETED
                && order.getStatus() != OrderConstant.STATUS_CANCELLED) {
            throw new BusinessException("只能删除已完成或已取消的订单");
        }
        orderItemMapper.delete(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        orderMapper.deleteById(orderId);
    }

    private OrderItem buildOrderItem(Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImage());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        return item;
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return time + random;
    }
}
