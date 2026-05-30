package com.xiaozhi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSaveDTO {
    private Long id;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String image;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String unit;

    private String description;

    private Integer status;
}
