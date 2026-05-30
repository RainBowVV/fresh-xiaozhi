package com.xiaozhi.controller.admin;

import com.xiaozhi.common.Result;
import com.xiaozhi.service.OrderService;
import com.xiaozhi.vo.OrderDetailVO;
import com.xiaozhi.vo.OrderVO;
import com.xiaozhi.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 订单分页列表（支持按状态筛选）
     */
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(orderService.pageAll(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.getDetailById(id));
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.ok();
    }
}
