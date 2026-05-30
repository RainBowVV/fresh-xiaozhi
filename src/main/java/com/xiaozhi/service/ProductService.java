package com.xiaozhi.service;

import com.xiaozhi.dto.ProductSaveDTO;
import com.xiaozhi.vo.PageResult;
import com.xiaozhi.vo.ProductDetailVO;
import com.xiaozhi.vo.ProductVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> listByCategoryId(Long categoryId);
    List<ProductVO> listByCategoryIdFromDB(Long categoryId);
    PageResult<ProductVO> pageFromDB(Long categoryId, Integer pageNum, Integer pageSize);
    ProductDetailVO getDetail(Long id);
    void save(ProductSaveDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
