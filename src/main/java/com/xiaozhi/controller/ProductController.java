package com.xiaozhi.controller;

import com.xiaozhi.common.Result;
import com.xiaozhi.service.ProductService;
import com.xiaozhi.vo.ProductDetailVO;
import com.xiaozhi.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 根据分类ID查询商品列表
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<ProductVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.ok(productService.listByCategoryId(categoryId));
    }

    /**
     * 根据商品ID查询商品详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.ok(productService.getDetail(id));
    }
}
