package com.xiaozhi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String remark;

    /**
     * Cart checkout: only create an order for these product IDs.
     * When empty, all cart items are used.
     */
    private List<Long> productIds;

    /**
     * Buy-now checkout: create an order directly from these item snapshots,
     * without requiring the products to already exist in the cart.
     */
    @Valid
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
