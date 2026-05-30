package com.xiaozhi.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.dto.LoginDTO;
import com.xiaozhi.entity.User;
import com.xiaozhi.mapper.UserMapper;
import com.xiaozhi.service.UserService;
import com.xiaozhi.util.JwtUtil;
import com.xiaozhi.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Value("${xiaozhi.wechat.appid}")
    private String appid;

    @Value("${xiaozhi.wechat.secret}")
    private String secret;

    @Override
    public String login(LoginDTO dto) {
        // 调用微信 jscode2session 接口换取 openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, dto.getCode());
        String result = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(result);

        // 检查微信返回的错误码
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = json.getStr("errmsg", "未知错误");
            throw new BusinessException(MessageConstant.WECHAT_LOGIN_FAIL + errmsg);
        }

        String openid = json.getStr("openid");
        if (openid == null) {
            throw new BusinessException(MessageConstant.WECHAT_LOGIN_NO_OPENID);
        }

        // 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            // 新用户，自动注册
            user = new User();
            user.setOpenid(openid);
            String nickname = (dto.getNickname() != null && !dto.getNickname().isEmpty()) ? dto.getNickname() : "微信用户";
            user.setNickname(nickname);
            user.setAvatarUrl(dto.getAvatarUrl() != null ? dto.getAvatarUrl() : "");
            userMapper.insert(user);
        } else {
            // 老用户，更新头像昵称
            boolean needUpdate = false;
            if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
                user.setNickname(dto.getNickname());
                needUpdate = true;
            }
            if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().equals(user.getAvatarUrl())) {
                user.setAvatarUrl(dto.getAvatarUrl());
                needUpdate = true;
            }
            if (needUpdate) {
                userMapper.updateById(user);
            }
        }

        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(MessageConstant.USER_NOT_FOUND);
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        return vo;
    }
}
