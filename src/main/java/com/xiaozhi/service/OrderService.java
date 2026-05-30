package com.xiaozhi.service;

import com.xiaozhi.dto.OrderCreateDTO;
import com.xiaozhi.vo.OrderDetailVO;
import com.xiaozhi.vo.OrderVO;
import com.xiaozhi.vo.PageResult;

import java.util.List;

public interface OrderService {
    OrderDetailVO create(Long userId, OrderCreateDTO dto);
    List<OrderVO> list(Long userId, List<Integer> statuses);
    List<OrderVO> listAll();
    PageResult<OrderVO> pageAll(Integer pageNum, Integer pageSize, Integer status);
    OrderDetailVO getDetail(Long userId, Long orderId);
    OrderDetailVO getDetailById(Long orderId);
    void cancel(Long userId, Long orderId);
    void pay(Long userId, Long orderId);
    void updateStatus(Long orderId, Integer status);
    void delete(Long userId, Long orderId);
}
