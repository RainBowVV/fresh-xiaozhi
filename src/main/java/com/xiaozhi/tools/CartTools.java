package com.xiaozhi.tools;

import com.xiaozhi.dto.CartAddDTO;
import com.xiaozhi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartTools {

    private final CartService cartService;

    @Tool(description = "添加商品到购物车")
    public void addToCart(@ToolParam(description = "商品ID") Long productId,
                          @ToolParam(description = "商品数量") Integer quantity,
                          ToolContext toolContext) {
        Long userId = Long.parseLong(toolContext.getContext().get("userId").toString());
        cartService.add(userId, new CartAddDTO(productId, quantity));
    }
}
