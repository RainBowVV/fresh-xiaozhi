package com.xiaozhi.controller;

import com.xiaozhi.common.Result;
import com.xiaozhi.service.CategoryService;
import com.xiaozhi.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取商品分类列表
     * @return 商品分类列表VO
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.list());
    }
}
