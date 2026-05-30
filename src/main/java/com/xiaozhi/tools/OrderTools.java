package com.xiaozhi.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaozhi.entity.Order;
import com.xiaozhi.mapper.OrderMapper;
import com.xiaozhi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderTools {

    private final OrderMapper orderMapper;

    @Tool(description = "根据订单状态获取订单信息")
    public List<Order> getOrderInfo(
            @ToolParam(description = "订单状态:0-待支付,1-已支付,2-备货中,3-配送中,4-已完成,5-已取消") Integer orderStatus) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getStatus, orderStatus));
    }
}
