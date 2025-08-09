package com.bryan.platform.service.user;

import com.bryan.platform.domain.entity.user.UserRole;
import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.exception.BusinessException;
import com.bryan.platform.repository.UserRepository;
import com.bryan.platform.repository.UserRoleRepository;
import com.bryan.platform.service.redis.RedisStringService;
import com.bryan.platform.util.http.HttpUtils;
import com.bryan.platform.util.jwt.JwtUtils;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.domain.request.auth.LoginRequest;
import com.bryan.platform.domain.request.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证服务类，处理注册、登录、鉴权、当前用户信息等逻辑。
 *
 * @author Bryan Long
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisStringService redisStringService;

    /* ---------- 注册 ---------- */

    /**
     * 用户注册。
     *
     * @param req 注册请求对象
     * @return 注册成功的用户实体
     * @throws BusinessException 用户名已存在
     * @throws BusinessException 插入数据库失败
     */
    @Transactional
    public User register(RegisterRequest req) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 查出默认角色
        UserRole defaultRole = userRoleRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new BusinessException("系统未配置默认角色"));

        // 构建用户实体，密码加密
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .phoneNumber(req.getPhoneNumber())
                .email(req.getEmail())
                .roles(defaultRole.getRoleName())
                .passwordResetTime(LocalDateTime.now())
                .build();

        // 插入用户数据
        user = userRepository.save(user);
        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    /* ---------- 登录 ---------- */

    /**
     * 用户登录，验证用户名和密码，生成 JWT Token。
     *
     * @param req 登录请求对象
     * @return 登录成功后的 JWT Token
     * @throws BusinessException 用户名不存在或密码错误
     */
    @Transactional
    public String login(LoginRequest req) {
        // 验证用户凭证
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            user.setLoginFailCount(user.getLoginFailCount() + 1);

            // 如果输入密码错误次数达到限额-硬编码为 5，则锁定账号
            if (user.getLoginFailCount() >= 5) {
                user.setStatus(UserStatusEnum.LOCKED);
                user.setAccountLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
            throw new BusinessException("用户名或密码错误");
        }

        // 已有有效 token 直接续期
        String oldToken = redisStringService.get(user.getUsername());
        if (oldToken != null && JwtUtils.validateToken(oldToken)) {
            redisStringService.setExpire(user.getUsername(), 86400);
            return oldToken;
        }

        // 更新登录信息
        user.setLoginTime(LocalDateTime.now());
        user.setLoginIp(HttpUtils.getClientIp());
        user.setLoginFailCount(0);
        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());
        String token = JwtUtils.generateToken(user.getId().toString(), claims);

        // 如果输入密码错误次数达到限额-硬编码为 5，则锁定账号
        redisStringService.set(user.getUsername(), token, 86400);
        return token;
    }

    /* ---------- 当前用户相关 ---------- */

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 当前用户 ID
     */
    public Long getCurrentUserId() {
        return JwtUtils.getCurrentUserId();
    }

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 当前用户名
     */
    public String getCurrentUsername() {
        return JwtUtils.getCurrentUsername();
    }

    /**
     * 获取当前登录用户的完整信息。
     *
     * @return 当前用户实体
     */
    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new BusinessException("当前用户不存在"));
    }

    /**
     * 判断用户是否具有管理员权限。
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return JwtUtils.getCurrentUserRoles().contains("ROLE_ADMIN");
    }

    /**
     * 判断用户是否具有管理员权限。
     *
     * @param userDetails 当前用户信息
     * @return 是否为管理员
     */
    public boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * 校验 JWT Token 是否有效。
     *
     * @param token 待校验的 Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
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
        return redisStringService.delete(getCurrentUsername());
    }

    /* ---------- Spring Security ---------- */

    /**
     * 根据用户名加载用户信息，用于 Spring Security 登录认证。
     *
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
}
