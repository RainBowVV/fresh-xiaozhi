package com.xiaozhi.service;

import com.xiaozhi.dto.CategorySaveDTO;
import com.xiaozhi.vo.CategoryVO;
import com.xiaozhi.vo.PageResult;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> list();
    List<CategoryVO> listFromDB();
    PageResult<CategoryVO> page(Integer pageNum, Integer pageSize);
    void save(CategorySaveDTO dto);
    void delete(Long id);
}
