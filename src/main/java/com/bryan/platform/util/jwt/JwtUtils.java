package com.bryan.platform.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 工具类，用于生成和解析 JWT 令牌。
 * <p>
 * 说明：
 * 1. 生成 Token 使用 HS256 签名算法，密钥长度至少256位。
 * 2. 解析 Token 时验证签名和有效期。
 * 3. 该类静态方法调用，无状态。
 * 4. 密钥 SECRET_STRING 建议生产环境中通过配置文件注入，避免硬编码。
 * </p>
 *
 * @author Bryan Long
 * @version v1.0
 * @since 2025/7/25
 */
public class JwtUtils {

    // 生产环境建议使用外部配置注入密钥，避免硬编码
    private static final String SECRET_STRING = "BryanTaoLong2025!@#SuperSecretKeyJwtToken987";

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION_MS = 86400000; // 24小时

    /**
     * 生成带额外声明的 JWT Token。
     *
     * @param userId 用户ID，作为 Token 的主题(subject)
     * @param claims 额外的声明数据，如角色、邮箱等
     * @return 生成的 JWT 字符串
     */
    public static String generateToken(String userId, Map<String, Object> claims) {
        // 1. 使用 Jwts.builder 创建 JWT 构造器
        // 2. 设置自定义 Claims
        // 3. 设置主题、签发时间、过期时间
        // 4. 使用 HS256 和密钥签名
        // 5. 生成并返回压缩后的 JWT 字符串
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成无额外声明的 JWT Token。
     *
     * @param userId 用户ID，作为 Token 的主题(subject)
     * @return 生成的 JWT 字符串
     */
    public static String generateToken(String userId) {
        return generateToken(userId, new HashMap<>());
    }

    /**
     * 从当前 HTTP 请求的 Authorization 头中解析并获取当前用户 ID。
     *
     * @return 当前用户ID（Long）
     * @throws RuntimeException 当请求缺少有效 Token 或 Token 解析失败时抛出
     */
    public static Long getCurrentUserId() {
        // 1. 获取当前请求的 ServletRequestAttributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();

        // 2. 从请求头获取 Authorization 字段
        String token = request.getHeader("Authorization");

        // 3. 验证 Token 格式是否正确
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 4. 解析 Token，验证签名和有效期
                String subject = Jwts.parser()
                        .verifyWith(SECRET_KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();

                // 5. 返回解析得到的用户ID（转换为 Long）
                return Long.parseLong(subject);
            } catch (Exception e) {
                throw new RuntimeException("Token 解析失败或无效: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("请求头中缺少 Authorization Token 或格式不正确。");
    }

    /**
     * 从当前 HTTP 请求的 Authorization 头中解析并获取当前用户 ID。
     *
     * @return 当前用户ID（Long）
     * @throws RuntimeException 当请求缺少有效 Token 或 Token 解析失败时抛出
     */
    public static String getCurrentUsername() {
        // 1. 获取当前请求的 ServletRequestAttributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();

        // 2. 从请求头获取 Authorization 字段
        String token = request.getHeader("Authorization");

        // 3. 验证 Token 格式是否正确
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 4. 解析 Token，验证签名和有效期
                Claims claims = Jwts.parser()
                        .verifyWith(SECRET_KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // 5. 返回解析得到的用户ID（转换为 Long）
                return claims.get("username").toString();
            } catch (Exception e) {
                throw new RuntimeException("Token 解析失败或无效: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("请求头中缺少 Authorization Token 或格式不正确。");
    }

    /**
     * 解析给定 Token 并返回其中的用户 ID (subject)。
     *
     * @param token JWT 字符串
     * @return 用户 ID 字符串
     * @throws RuntimeException Token 解析失败时抛出
     */
    public static String getUserIdFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token 解析失败或无效: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Token 中获取所有 Claims。
     *
     * @param token JWT 字符串
     * @return Claims 对象
     * @throws RuntimeException 解析失败时抛出
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("无法从 Token 获取 Claims: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Token 的 Claims 中提取角色列表。
     * 假设角色以逗号分隔字符串存储在 "roles" 声明中。
     *
     * @param token JWT 字符串
     * @return 角色字符串列表（确保带 "ROLE_" 前缀）
     */
    public static String getUsernameFromTokenClaims(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从 Token 的 Claims 中提取角色列表。
     * 假设角色以逗号分隔字符串存储在 "roles" 声明中。
     *
     * @param token JWT 字符串
     * @return 角色字符串列表（确保带 "ROLE_" 前缀）
     */
    public static List<String> getRolesFromTokenClaims(String token) {
        Claims claims = getClaimsFromToken(token);
        String rolesString = (String) claims.get("roles");
        if (rolesString != null && !rolesString.isEmpty()) {
            return Arrays.stream(rolesString.split(","))
                    .map(String::trim)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 验证 Token 是否有效（格式正确且签名合法且未过期）。
     *
     * @param token JWT 字符串
     * @return true 表示有效，false 表示无效或过期
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

