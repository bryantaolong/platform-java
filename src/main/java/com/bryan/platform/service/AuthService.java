package com.bryan.platform.service;

import com.bryan.platform.common.exception.BusinessException;
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
 * 用户认证服务类，处理注册、登录、鉴权、当前用户信息等逻辑。
 *
 * @author Bryan Long
 * @version 2.0
 * @since 2025/6/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册。
     *
     * @param registerRequest 注册请求对象
     * @return 注册成功的用户实体
     * @throws RuntimeException 用户名已存在
     * @throws BusinessException 插入数据库失败
     */
    public User register(RegisterRequest registerRequest) {
        // 1. 检查用户名是否已存在
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 构建用户实体，密码加密，设置默认角色和初始状态
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .status(0)
                .roles("ROLE_USER")
                .build();

        // 3. 插入用户数据
        int inserted = userMapper.insert(user);
        if (inserted == 0) {
            throw new BusinessException("插入数据库失败");
        }

        // 4. 返回新注册用户
        return user;
    }

    /**
     * 用户登录，验证用户名和密码，生成 JWT Token。
     *
     * @param loginRequest 登录请求对象
     * @return 登录成功后的 JWT Token
     * @throws RuntimeException 用户名不存在或密码错误
     */
    public String login(LoginRequest loginRequest) {
        // 1. 根据用户名查询用户
        User user = userMapper.selectByUsername(loginRequest.getUsername());

        // 2. 验证密码是否正确
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 构建 JWT claims，包含用户角色
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());

        // 4. 生成并返回 Token
        return JwtUtil.generateToken(user.getId().toString(), claims);
    }

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 当前用户 ID
     */
    public Long getCurrentUserId() {
        // 1. 从 JWT Token 中提取用户 ID
        return JwtUtil.getCurrentUserId();
    }

    /**
     * 获取当前登录用户的完整信息。
     *
     * @return 当前用户实体
     */
    public User getCurrentUser() {
        // 1. 获取当前用户 ID
        Long userId = JwtUtil.getCurrentUserId();

        // 2. 查询数据库返回用户信息
        return userMapper.selectById(userId);
    }

    /**
     * 判断用户是否具有管理员权限。
     *
     * @param userDetails 当前用户信息
     * @return 是否为管理员
     */
    public boolean isAdmin(UserDetails userDetails) {
        // 1. 遍历用户权限，判断是否包含 ROLE_ADMIN
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * 校验 JWT Token 是否有效。
     *
     * @param token 待校验的 Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        // 1. 调用工具类验证 Token 合法性
        return JwtUtil.validateToken(token);
    }

    /**
     * 根据用户名加载用户信息，用于 Spring Security 登录认证。
     *
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据用户名查询用户
        User user = userMapper.selectByUsername(username);

        // 2. 用户不存在则抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 3. 返回用户详情（已实现 UserDetails）
        return user;
    }
}
