package com.bryan.platform.service.user;

import com.bryan.platform.common.enums.UserStatusEnum;
import com.bryan.platform.common.exception.BusinessException;
import com.bryan.platform.repository.UserRepository;
import com.bryan.platform.service.redis.RedisStringService;
import com.bryan.platform.util.http.HttpUtils;
import com.bryan.platform.util.jwt.JwtUtils;
import com.bryan.platform.model.entity.user.User;
import com.bryan.platform.model.request.auth.LoginRequest;
import com.bryan.platform.model.request.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户认证服务类，处理注册、登录、鉴权、当前用户信息等逻辑。
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/6/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisStringService redisStringService;

    /**
     * 用户注册。
     *
     * @param registerRequest 注册请求对象
     * @return 注册成功的用户实体
     * @throws BusinessException 用户名已存在
     * @throws BusinessException 插入数据库失败
     */
    public User register(RegisterRequest registerRequest) {
        // 1. 检查用户名是否已存在
        if(userRepository.findByUsername(registerRequest.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 构建用户实体，密码加密
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .email(registerRequest.getEmail())
                .roles("ROLE_USER")
                .passwordResetTime(LocalDateTime.now())
                .createBy(registerRequest.getUsername())
                .updateBy(registerRequest.getUsername())
                .build();

        // 3. 插入用户数据
        User saved = userRepository.save(user);
        if (saved == null) {
            throw new BusinessException("插入数据库失败");
        }

        log.info("用户注册成功: id: {}, username: {} ", user.getId(), user.getUsername());

        // 4. 返回新注册用户
        return user;
    }

    /**
     * 用户登录，验证用户名和密码，生成 JWT Token。
     *
     * @param loginRequest 登录请求对象
     * @return 登录成功后的 JWT Token
     * @throws BusinessException 用户名不存在或密码错误
     */
    public String login(LoginRequest loginRequest) {
        // 1. 验证用户凭证
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            user.setLoginFailCount(user.getLoginFailCount() + 1);

            // 如果输入密码错误次数达到限额-硬编码为 5，则锁定账号
            if(user.getLoginFailCount() >= 5) {
                user.setStatus(UserStatusEnum.NORMAL);
                user.setAccountLockTime(LocalDateTime.now());
                throw new BusinessException("输入密码错误次数过多，账号锁定");
            }
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 检查现有Token（使用JwtUtils验证有效性）
        String existingToken = redisStringService.get(user.getUsername());
        if (existingToken != null && JwtUtils.validateToken(existingToken)) {
            // 刷新Redis中的Token过期时间
            redisStringService.setExpire(user.getUsername(), 86400000 / 1000);
            return existingToken;
        }

        // 3. 更新用户登录信息
        user.setLoginTime(LocalDateTime.now());
        user.setLoginIp(HttpUtils.getClientIp());
        user.setLoginFailCount(0); // 重置密码输入错误次数
        userRepository.save(user);

        // 4. 生成新的JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());

        String token = JwtUtils.generateToken(user.getId().toString(), claims);

        // 5. 存储到Redis（设置与JWT相同的过期时间）
        boolean saved = redisStringService.set(
                user.getUsername(),
                token,
                86400000 / 1000
        );

        if (!saved) {
            throw new BusinessException("Token存储失败");
        }

        return token;
    }

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 当前用户 ID
     */
    public Long getCurrentUserId() {
        // 1. 从 JWT Token 中提取用户 ID
        return JwtUtils.getCurrentUserId();
    }

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 当前用户名
     */
    public String getCurrentUsername() {
        // 1. 从 JWT Token 中提取用户名
        return JwtUtils.getCurrentUsername();
    }

    /**
     * 获取当前登录用户的完整信息。
     *
     * @return 当前用户实体
     */
    public User getCurrentUser() {
        // 1. 获取当前用户 ID
        Long userId = JwtUtils.getCurrentUserId();

        // 2. 查询数据库返回用户信息
        return userRepository.findById(userId);
    }

    /**
     * 判断用户是否具有管理员权限。
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        // 1. 调用 JwtUtils 获取用户权限列表
        List<String> roles = JwtUtils.getCurrentUserRoles();

        // 2. 遍历用户权限，判断是否包含 ROLE_ADMIN
        for (String role : roles) {
            if ("ROLE_ADMIN".equals(role)) {
                return true;
            }
        }
        return false;
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
        return JwtUtils.validateToken(token);
    }

    /**
     * 刷新当前用户的 JWT Token。
     *
     * @return String 是否有效
     */
    public String refreshToken() {
        // 1. 获取当前用户信息
        User user = getCurrentUser();

        // 2. 构建 JWT claims，包含用户角色
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());

        // 3. 生成并返回 Token
        return JwtUtils.generateToken(user.getId().toString(), claims);
    }

    /**
     * 清除当前用户的 JWT Token 在 Redis 中的缓存。
     *
     * @return boolean 是否成功
     * @throws BusinessException Token清理失败
     */
    public boolean logout() {
        String username = JwtUtils.getCurrentUsername();
        boolean deleted = redisStringService.delete(username);
        if (!deleted) {
            throw new BusinessException("Token清除失败");
        }

        return true;
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
        User user = userRepository.findByUsername(username);

        // 2. 用户不存在则抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 3. 返回用户详情（已实现 UserDetails）
        return user;
    }
}
