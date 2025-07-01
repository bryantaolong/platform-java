package com.bryan.platform.service;

import com.bryan.platform.util.JwtUtil;
import com.bryan.platform.dao.mapper.UserMapper;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.request.LoginRequest;
import com.bryan.platform.model.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: AuthService
 * Package: com.bryan.platform.service
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/28 - 14:18
 * Version: v1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册逻辑。
     *
     * @param registerRequest 用户注册数据传输对象
     * @return 注册成功的用户实体
     * @throws RuntimeException 如果用户名已存在
     */
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 构建用户实体，密码进行加密，默认状态为0，默认角色为 ROLE_USER
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .status(0)
                .roles("ROLE_USER") // 注册时默认赋予 ROLE_USER 角色
                .build();
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录逻辑。
     *
     * @param loginRequest 用户登录数据传输对象
     * @return JWT Token 字符串
     * @throws RuntimeException 如果用户名或密码错误
     */
    public String login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        // 验证用户名和密码
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // --- 关键修改：在生成 JWT Token 时包含用户角色信息 ---
        Map<String, Object> claims = new HashMap<>();
        // 将用户的角色字符串添加到 JWT Claims 中
        // 注意：这里直接将 roles 字符串放入，JwtUtil会将其转换为List<String>
        claims.put("roles", user.getRoles());
        // 可以根据需要添加其他信息，例如 email
        // claims.put("email", user.getEmail());

        // 使用 JwtUtil 生成包含 userId 和 roles 的 Token
        return JwtUtil.generateToken(user.getId().toString(), claims);
    }

    /**
     * 获取当前登录用户实体。
     *
     * @return 当前登录用户实体
     */
    public User getCurrentUser() {
        Long userId = JwtUtil.getCurrentUserId();
        return userMapper.selectById(userId);
    }

    /**
     * 验证 Token 的有效性
     * @param token JWT Token
     * @return true 如果 Token 有效，false 如果无效
     */
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token);
    }

    /**
     * 实现 UserDetailsService 接口方法，用于 Spring Security 加载用户详情。
     *
     * @param username 用户名
     * @return 用户详情 (UserDetails 实例)
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user; // user 实体已经实现了 UserDetails 接口，并正确解析了 roles
    }
}
