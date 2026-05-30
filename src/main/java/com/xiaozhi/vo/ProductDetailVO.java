package com.xiaozhi.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDetailVO {
    private Long id;
    private Long categoryId;
    private String name;
    private String image;
    private BigDecimal price;
    private String unit;
    private String description;
}
