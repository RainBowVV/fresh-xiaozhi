package com.xiaozhi.controller;

import com.xiaozhi.annotation.RequireAuth;
import com.xiaozhi.common.Result;
import com.xiaozhi.dto.LoginDTO;
import com.xiaozhi.service.UserService;
import com.xiaozhi.util.UserContext;
import com.xiaozhi.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
/**
 * 用户模块
 */
public class UserController {

    private final UserService userService;

    /**
     * 微信登录
     * @param dto
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return Result.ok(token);
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @RequireAuth
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.ok(userService.getUserInfo(UserContext.getUserId()));
    }
}
