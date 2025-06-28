package com.bryan.platform.common.util;

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
 * ClassName: JwtUtil
 * Package: com.bryan.platform.common.util
 * Description: JWT 工具类，用于生成和解析 JWT 令牌。
 * Author: Bryan Long
 * Create: 2025/6/19 - 19:52
 * Version: v1.0
 */
public class JwtUtil {

    // 密钥字符串。在实际生产环境中，此密钥应从安全配置中读取，并且足够复杂。
    // 对于 JJWT 0.12.x 版本，密钥需要是字节数组才能创建 Key 对象。
    private static final String SECRET_STRING = "ThisIsAVerySecretKeyForYourJWTAuthenticationAndItShouldBeLongEnough"; // 确保密钥足够长，至少256位（32字节）用于HS256算法

    // 将字符串密钥转换为 SecretKey 对象，用于签名和验证。
    // Keys.hmacShaKeyFor 方法返回一个 SecretKey 对象，该对象实现了 java.security.Key 接口。
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Token 有效期（毫秒），这里设置为24小时。
    private static final long EXPIRATION_MS = 86400000;

    /**
     * 生成一个带有主体 (userId) 和额外声明 (claims) 的 JWT Token。
     *
     * @param userId 要设置为 Token 主体的用户ID。
     * @param claims 包含在 Token 中的额外声明 (例如，角色、邮箱等)。
     * @return 生成的 JWT 字符串。
     */
    public static String generateToken(String userId, Map<String, Object> claims) {
        // 使用 Jwts.builder() 构建 JWT。
        // signWith 方法在 0.11.x+ 版本中接受 Key 对象和 SignatureAlgorithm（签名算法）。
        return Jwts.builder()
                .setClaims(claims) // 设置自定义 Claims
                .setSubject(userId) // 设置主题（通常是用户ID）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) // 设置过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 使用 HS256 算法和密钥进行签名
                .compact(); // 压缩并序列化生成 JWT 字符串
    }

    /**
     * 重载方法：生成一个不带额外声明的 Token（为了向后兼容，但不推荐在生产环境使用）。
     *
     * @param userId 要设置为 Token 主体的用户ID。
     * @return 生成的 JWT 字符串。
     */
    public static String generateToken(String userId) {
        return generateToken(userId, new HashMap<>()); // 调用带 claims 参数的重载方法
    }


    /**
     * 从当前请求中获取当前用户的 ID。
     *
     * @return 当前用户的 ID。
     * @throws RuntimeException 如果 Token 无效或缺失。
     */
    public static Long getCurrentUserId() {
        // 从 RequestContextHolder 获取当前请求的 ServletRequestAttributes。
        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()); // 优化：使用 Objects.requireNonNull 避免空指针检查
        HttpServletRequest request = attributes.getRequest();

        // 从请求头中获取 Authorization 字段。
        String token = request.getHeader("Authorization");

        // 检查 Token 是否存在且以 "Bearer " 开头。
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 截取掉 "Bearer " 前缀
            try {
                // 使用 Jwts.parser() 解析 Token。
                // verifyWith 方法在 0.11.x+ 版本中用于设置验证密钥。
                // build() 方法在 0.11.x+ 版本中是必需的，用于构建 JwtParser。
                // parseClaimsJws().getBody().getSubject() 用于获取主体（用户ID）。
                String subject = Jwts.parser()
                        .verifyWith(SECRET_KEY) // 设置用于验证的密钥
                        .build() // 构建 JwtParser
                        .parseClaimsJws(token) // 解析 JWS (Signed JWT)
                        .getBody() // 获取 Claims Body
                        .getSubject(); // 获取主体
                return Long.parseLong(subject); // 将主体（用户ID）转换为 Long 类型
            } catch (Exception e) {
                // 捕获所有解析异常（如签名无效、过期等）
                throw new RuntimeException("Token 解析失败或无效: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("请求头中缺少 Authorization Token 或格式不正确。");
    }

    /**
     * 解析 Token 获取用户 ID。
     * 此方法适用于直接传入 Token 字符串的场景。
     *
     * @param token JWT Token 字符串。
     * @return 用户 ID。
     * @throws RuntimeException 如果 Token 无效。
     */
    public static String parseToken(String token) {
        try {
            // 逻辑与 getCurrentUserId() 类似，但直接使用传入的 Token。
            return Jwts.parser()
                    .verifyWith(SECRET_KEY) // 设置用于验证的密钥
                    .build() // 构建 JwtParser
                    .parseClaimsJws(token) // 解析 JWS
                    .getBody() // 获取 Claims Body
                    .getSubject(); // 获取主体
        } catch (Exception e) {
            // 捕获所有解析异常
            throw new RuntimeException("Token 解析失败或无效: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Token 中获取 Claims。
     *
     * @param token JWT Token 字符串。
     * @return Claims 对象，包含 Token 中的所有声明。
     * @throws RuntimeException 如果 Token 无效。
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
     * 从 Token 中获取角色列表。
     *
     * @param token JWT Token 字符串。
     * @return 角色字符串列表。
     */
    public static List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String rolesString = (String) claims.get("roles"); // 假设角色以逗号分隔的字符串形式存储在 claims 中
        if (rolesString != null && !rolesString.isEmpty()) {
            return Arrays.stream(rolesString.split(","))
                    .map(String::trim)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // 确保添加 ROLE_ 前缀
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 验证 JWT Token 的有效性。
     *
     * @param token JWT Token 字符串
     * @return true 如果 Token 有效，false 如果无效
     */
    public static boolean validateToken(String token) {
        try {
            // 解析 Token，如果解析成功且未抛出异常，则说明 Token 有效
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
