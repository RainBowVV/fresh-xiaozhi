package com.xiaozhi.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private AddressVO address;
    private List<CartItemVO> items;
}
