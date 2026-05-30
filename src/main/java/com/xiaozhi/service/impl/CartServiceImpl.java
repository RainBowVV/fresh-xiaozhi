package com.xiaozhi.service.impl;

import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.constant.RedisKeyConstant;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.dto.CartAddDTO;
import com.xiaozhi.dto.CartUpdateDTO;
import com.xiaozhi.entity.Order;
import com.xiaozhi.entity.OrderItem;
import com.xiaozhi.entity.Product;
import com.xiaozhi.mapper.OrderItemMapper;
import com.xiaozhi.mapper.OrderMapper;
import com.xiaozhi.mapper.ProductMapper;
import com.xiaozhi.service.CartService;
import com.xiaozhi.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    private static final String CART_KEY_PREFIX = RedisKeyConstant.CART_KEY_PREFIX;

    @Override
    public void add(Long userId, CartAddDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND_OR_OFFLINE);
        }
        String key = CART_KEY_PREFIX + userId;
        String field = String.valueOf(dto.getProductId());
        // 累加数量，而非覆盖
        redisTemplate.opsForHash().increment(key, field, dto.getQuantity());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CartItemVO> list(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询所有商品
        List<Long> productIds = entries.keySet().stream()
                .map(k -> Long.parseLong(k.toString()))
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<CartItemVO> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            Integer quantity = Integer.parseInt(entry.getValue().toString());

            Product product = productMap.get(productId);
            if (product == null) {
                redisTemplate.opsForHash().delete(key, entry.getKey());
                continue;
            }

            CartItemVO vo = new CartItemVO();
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setPrice(product.getPrice());
            vo.setUnit(product.getUnit());
            vo.setQuantity(quantity);
            items.add(vo);
        }
        return items;
    }

    @Override
    public void update(Long userId, CartUpdateDTO dto) {
        String key = CART_KEY_PREFIX + userId;
        if (!redisTemplate.opsForHash().hasKey(key, String.valueOf(dto.getProductId()))) {
            throw new BusinessException(MessageConstant.CART_PRODUCT_NOT_FOUND);
        }
        redisTemplate.opsForHash().put(key,
                String.valueOf(dto.getProductId()),
                dto.getQuantity());
    }

    @Override
    public void delete(Long userId, Long productId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
    }

    @Override
    public void clear(Long userId) {
        redisTemplate.delete(CART_KEY_PREFIX + userId);
    }

    @Override
    public void reorder(Long userId, Long orderId) {
        // 1. 校验订单归属
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 2. 查询订单明细
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId));
        if (orderItems.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 3. 批量查询商品
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        Set<Long> validIds = productMapper.selectBatchIds(productIds).stream()
                .filter(p -> p.getStatus() == 1)
                .map(Product::getId)
                .collect(Collectors.toSet());

        // 4. 批量加入购物车
        String key = CART_KEY_PREFIX + userId;
        int count = 0;
        for (OrderItem item : orderItems) {
            if (!validIds.contains(item.getProductId())) {
                continue;
            }
            redisTemplate.opsForHash().increment(key, String.valueOf(item.getProductId()), item.getQuantity());
            count++;
        }

        if (count == 0) {
            throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND_OR_OFFLINE);
        }
    }
}
