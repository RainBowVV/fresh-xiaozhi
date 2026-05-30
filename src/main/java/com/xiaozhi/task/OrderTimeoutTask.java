package com.xiaozhi.task;

import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.constant.OrderConstant;
import com.xiaozhi.constant.RedisKeyConstant;
import com.xiaozhi.entity.Order;
import com.xiaozhi.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderMapper orderMapper;

    /**
     * 每 30 秒扫描一次 Redis 延迟队列，取消超时订单
     */
    @Scheduled(fixedRate = 30000)
    public void cancelTimeoutOrders() {
        long now = System.currentTimeMillis();
        log.info(now + ": 正在扫描Redis延时队列");

        // 获取所有已过期的订单（score <= 当前时间）
        Set<Object> expiredOrderIds = redisTemplate.opsForZSet()
                .rangeByScore(RedisKeyConstant.ORDER_TIMEOUT_KEY, 0, now);

        if (expiredOrderIds == null || expiredOrderIds.isEmpty()) {
            return;
        }

        for (Object orderIdObj : expiredOrderIds) {
            Long orderId = Long.parseLong(orderIdObj.toString());

            // 原子移除，返回移除数量；为 0 表示已被其他实例处理
            Long removed = redisTemplate.opsForZSet().remove(RedisKeyConstant.ORDER_TIMEOUT_KEY, orderId);
            if (removed == null || removed == 0) {
                continue;
            }

            // 查询订单，仅处理待付款状态
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getStatus() != OrderConstant.STATUS_PENDING_PAYMENT) {
                continue;
            }

            // 取消订单
            order.setStatus(OrderConstant.STATUS_CANCELLED);
            order.setRemark(MessageConstant.ORDER_TIMEOUT_CANCEL);
            orderMapper.updateById(order);

            log.info("订单超时自动取消，orderId={}", orderId);
        }
    }
}
