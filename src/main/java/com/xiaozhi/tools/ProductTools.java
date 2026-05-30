package com.xiaozhi.tools;

import com.xiaozhi.service.ProductService;
import com.xiaozhi.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductTools {

    private final ProductService productService;

    @Tool(description = "根据商品分类ID获取商品信息")
    public List<ProductVO> getProductInfo(
            @ToolParam(description = "商品分类ID") Long categoryId) {
        return productService.listByCategoryId(categoryId);
    }
}
