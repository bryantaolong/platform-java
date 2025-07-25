package com.bryan.platform.filter;

import com.bryan.platform.model.response.Result;
import com.bryan.platform.common.enums.ErrorCode;
import com.bryan.platform.util.jwt.JwtUtil;
import com.bryan.platform.model.entity.User;
import com.bryan.platform.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority; // 新增导入
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 新增导入

import java.io.IOException;
import java.util.Collection; // 新增导入
import java.util.List; // 新增导入
import java.util.stream.Collectors; // 新增导入

/**
 * JWT 认证过滤器，用于解析Token并设置Spring Security上下文。
 *
 * @author Bryan Long
 * @since 2025/6/19 - 20:02
 * @version 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 推荐使用构造器注入，Spring 会自动处理。
    // @Autowired 注解也可以，但构造器注入是更好的实践。
    private final UserService userService;

    private final ObjectMapper objectMapper;

    // 构造器注入
    public JwtAuthenticationFilter(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        // 如果没有 Token 或者 Token 格式不正确，则直接放行，让后续的 Security 配置处理（例如匿名访问或认证失败）
        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7); // 截取掉 "Bearer " 前缀

        try {
            // --- 关键修改：从 Token 的 Claims 中直接获取角色信息 ---
            Long userId = Long.parseLong(JwtUtil.parseToken(token)); // 获取用户ID

            // 从 Token 的 Claims 中获取角色列表
            List<String> roles = JwtUtil.getRolesFromToken(token);
            // 将角色字符串列表转换为 Spring Security 的 GrantedAuthority 列表
            Collection<? extends GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 这里不再需要 userService.getUserById(userId) 来获取角色，
            // 权限信息直接从 Token 中获取，提高性能并符合JWT无状态原则。
            // 但为了安全起见，通常还会从数据库加载用户主体（User对象），以验证用户状态等。
            // 这里我们仍然从数据库加载用户，以确保用户是存在的且状态正常。
            User user = userService.getUserById(userId);
            if (user == null || !user.isEnabled() || !user.isAccountNonLocked()) {
                // 如果用户不存在或被禁用/锁定，则视为认证失败
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                        objectMapper.writeValueAsString(
                                Result.error(ErrorCode.UNAUTHORIZED, "用户状态异常或不存在")
                        )
                );
                return;
            }

            // 构建认证对象，使用从 Token 和数据库验证后的权限
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user, null, authorities // 使用从 Token Claims 获取的权限
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 处理 Token 无效或过期的情况
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Result.error(ErrorCode.UNAUTHORIZED, "Token无效或已过期: " + e.getMessage())
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
