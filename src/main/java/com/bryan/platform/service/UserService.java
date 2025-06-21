package com.bryan.platform.service;

import com.bryan.platform.model.entity.User;
import com.bryan.platform.model.dto.LoginDTO;
import com.bryan.platform.model.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * ClassName: UserService
 * Package: com.bryan.platform.service
 * Description: 用户服务接口。
 * 定义了用户相关的核心业务操作，包括用户注册、登录、信息查询等。
 * 同时，它也扩展了 Spring Security 的 UserDetailsService 职责，用于加载用户详情。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:49
 * Version: v1.0
 */
public interface UserService {
    /**
     * 注册新用户。
     *
     * @param userDTO 包含注册用户信息的DTO。
     * @return 注册成功的用户实体。
     */
    User register(UserDTO userDTO);

    /**
     * 用户登录。
     *
     * @param loginDTO 包含用户登录凭据的DTO（用户名和密码）。
     * @return 登录成功后生成的JWT Token字符串。
     */
    String login(LoginDTO loginDTO);

    /**
     * 获取当前已认证的用户信息。
     *
     * @return 当前用户的实体。
     */
    User getCurrentUser();

    /**
     * 根据用户ID获取用户实体。
     *
     * @param userId 要查询的用户ID。
     * @return 对应ID的用户实体。
     */
    User getUserById(Long userId);

    /**
     * 根据用户名加载用户详情，用于 Spring Security 认证。
     * 此方法也暴露在 UserService 接口中，方便其他服务调用以获取完整的 User 对象。
     *
     * @param username 用户名。
     * @return 用户详情 (User 实体，已实现 UserDetails 接口)。
     * @throws UsernameNotFoundException 如果用户不存在。
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
