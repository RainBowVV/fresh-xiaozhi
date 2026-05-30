package com.xiaozhi.tools;

import com.xiaozhi.service.CategoryService;
import com.xiaozhi.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryTools {

    private final CategoryService categoryService;

    @Tool(description = "查询所有商品分类")
    public List<CategoryVO> getAllCategories() {
        return categoryService.list();
    }
}
