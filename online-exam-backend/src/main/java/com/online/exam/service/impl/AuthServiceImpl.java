package com.online.exam.service.impl;

import com.online.exam.common.BusinessException;
import com.online.exam.common.ResultCode;
import com.online.exam.dto.LoginDTO;
import com.online.exam.dto.RegisterDTO;
import com.online.exam.entity.User;
import com.online.exam.mapper.UserMapper;
import com.online.exam.security.JwtUtil;
import com.online.exam.security.SecurityUser;
import com.online.exam.service.AuthService;
import com.online.exam.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String role = SecurityUser.normalizeRole(user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), List.of(role));

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setRole(role);
        return vo;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existing = userMapper.selectByUsername(registerDTO.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        String role = registerDTO.getRole() == null ? "STUDENT" : SecurityUser.normalizeRole(registerDTO.getRole());
        if (!List.of("STUDENT", "TEACHER").contains(role)) {
            throw new BusinessException("注册角色不合法");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setName(registerDTO.getName() != null ? registerDTO.getName() : registerDTO.getUsername());
        user.setRole(role);
        userMapper.insert(user);
    }

    @Override
    public LoginVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setRole(SecurityUser.normalizeRole(user.getRole()));
        return vo;
    }
}
