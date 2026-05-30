package com.xiaozhi.controller;

import com.xiaozhi.common.Result;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.dto.AdminLoginDTO;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JwtUtil jwtUtil;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        if (!adminUsername.equals(dto.getUsername()) || !adminPassword.equals(dto.getPassword())) {
            throw new BusinessException(MessageConstant.ADMIN_LOGIN_FAIL);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generateToken(0L));
        data.put("username", adminUsername);
        return Result.ok(data);
    }
}
