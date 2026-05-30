package com.xiaozhi.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private String unit;
    private Integer quantity;
}
