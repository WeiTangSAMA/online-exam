package com.online.exam.service;

import com.online.exam.dto.LoginDTO;
import com.online.exam.dto.RegisterDTO;
import com.online.exam.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    LoginVO getCurrentUser(Long userId);
}
