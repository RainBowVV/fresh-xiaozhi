package com.xiaozhi.controller;

import com.xiaozhi.annotation.RequireAuth;
import com.xiaozhi.common.Result;
import com.xiaozhi.dto.CartAddDTO;
import com.xiaozhi.dto.CartUpdateDTO;
import com.xiaozhi.service.CartService;
import com.xiaozhi.util.UserContext;
import com.xiaozhi.vo.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车
     * @param dto
     * @return
     */
    @RequireAuth
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.add(UserContext.getUserId(), dto);
        return Result.ok();
    }

    /**
     * 获取购物车商品列表
     * @return
     */
    @RequireAuth
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list(UserContext.getUserId()));
    }

    /**
     * 更新购物车商品数量
     * @param dto
     * @return
     */
    @RequireAuth
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO dto) {
        cartService.update(UserContext.getUserId(), dto);
        return Result.ok();
    }

    /**
     * 根据商品ID删除购物车商品
     * @param productId 商品ID
     * @return
     */
    @RequireAuth
    @DeleteMapping("/delete/{productId}")
    public Result<Void> delete(@PathVariable Long productId) {
        cartService.delete(UserContext.getUserId(), productId);
        return Result.ok();
    }

    /**
     * 清空购物车
     * @return
     */
    @RequireAuth
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 再次下单时，根据订单ID重新排序购物车商品
     * @param orderId 订单ID
     * @return
     */
    @RequireAuth
    @PostMapping("/reorder/{orderId}")
    public Result<Void> reorder(@PathVariable Long orderId) {
        cartService.reorder(UserContext.getUserId(), orderId);
        return Result.ok();
    }
}
