package com.xiaozhi.service;

import com.xiaozhi.dto.LoginDTO;
import com.xiaozhi.vo.UserVO;

public interface UserService {
    String login(LoginDTO dto);
    UserVO getUserInfo(Long userId);
}
