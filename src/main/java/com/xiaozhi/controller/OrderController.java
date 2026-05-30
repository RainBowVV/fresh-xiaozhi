package com.xiaozhi.controller;

import com.xiaozhi.annotation.RequireAuth;
import com.xiaozhi.common.Result;
import com.xiaozhi.dto.OrderCreateDTO;
import com.xiaozhi.service.OrderService;
import com.xiaozhi.util.UserContext;
import com.xiaozhi.vo.OrderDetailVO;
import com.xiaozhi.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * @param dto
     * @return
     */
    @RequireAuth
    @PostMapping("/create")
    public Result<OrderDetailVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(orderService.create(UserContext.getUserId(), dto));
    }

    /**
     * 查询用户订单列表（支持按状态筛选）
     * @param statuses 订单状态列表，不传则查全部
     * @return 订单列表
     */
    @RequireAuth
    @GetMapping("/list")
    public Result<List<OrderVO>> list(@RequestParam(required = false) List<Integer> statuses) {
        return Result.ok(orderService.list(UserContext.getUserId(), statuses));
    }

    /**
     * 根据订单ID查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @RequireAuth
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.getDetail(UserContext.getUserId(), id));
    }

    /**
     * 根据订单ID取消订单
     * @param id 订单ID
     * @return 无返回值
     */
    @RequireAuth
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(UserContext.getUserId(), id);
        return Result.ok();
    }

    /**
     * 模拟支付订单
     * @param id 订单ID
     * @return 无返回值
     */
    @RequireAuth
    @PostMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(UserContext.getUserId(), id);
        return Result.ok();
    }

    /**
     * 根据订单ID删除订单
     * @param id 订单ID
     * @return 无返回值
     */
    @RequireAuth
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }
}
