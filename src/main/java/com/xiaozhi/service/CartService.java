package com.xiaozhi.service;

import com.xiaozhi.dto.CartAddDTO;
import com.xiaozhi.dto.CartUpdateDTO;
import com.xiaozhi.vo.CartItemVO;
import java.util.List;

public interface CartService {
    void add(Long userId, CartAddDTO dto);
    List<CartItemVO> list(Long userId);
    void update(Long userId, CartUpdateDTO dto);
    void delete(Long userId, Long productId);
    void clear(Long userId);
    void reorder(Long userId, Long orderId);
}
