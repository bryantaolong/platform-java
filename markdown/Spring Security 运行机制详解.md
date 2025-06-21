# Spring Security 运行机制详解
Spring Security 是一个功能强大且高度可定制的认证和授权框架。在本项目中，它主要通过 JWT (JSON Web Token) 来实现无状态的认证和授权。

## 核心概念概览
1. 认证 (Authentication): 验证用户身份的过程。用户提供凭据（如用户名和密码），系统验证这些凭据是否有效。

2. 授权 (Authorization): 验证用户是否有权执行某个操作或访问某个资源的过程。在认证成功后进行。

3. `SecurityContextHolder`: Spring Security 存储当前认证用户信息的上下文。在请求处理过程中，认证信息（`Authentication`
对象）会被保存在这里，以便后续的授权检查。

4. `Authentication` 对象: 代表一个已认证或正在认证的用户主体。它包含用户的身份信息、凭据和所拥有的权限（`GrantedAuthority`）。

5. `UserDetails` 接口: 提供核心用户信息的接口，你的 `User` 实体实现了它。Spring Security 通过它来获取用户的用户名、密码、账户状态和权限列表。

6. `UserDetailsService` 接口: 用于加载 `UserDetails` 对象的接口，你的 `UserServiceImpl` 实现了它。当用户尝试认证时，Spring Security
会调用它的 `loadUserByUsername` 方法来获取用户详情。

7. `PasswordEncoder`: 用于加密和匹配密码的接口，你的 `BCryptPasswordEncoder` 是其实现。

8. 过滤器链 (Filter Chain): Spring Security 通过一系列的 Servlet Filter 来拦截 HTTP 请求，执行认证、授权等操作。

## 项目中的 Spring Security 运行流程

### 1. 用户认证 (登录) 流程
当用户调用 `/api/user/login` 接口进行登录时，流程如下：

1. **请求登录**: 客户端（如 Postman）发送 POST 请求到 `/api/user/login`，携带 `username` 和 `password`。

2. `UserController.login()`: 接收到请求，调用 `userService.login(userLoginDTO)`。

3. `UserServiceImpl.login()`:
   -
   - 通过 `userMapper.selectByUsername()` 从数据库查询用户。
   
   - 使用 `passwordEncoder.matches()` 比较用户输入的密码和数据库中加密后的密码。
   
   - 如果验证成功，调用 `JwtUtil.generateToken(user.getId().toString(), claims)`。 
     - 关键点： 在这里，`UserServiceImpl` 从数据库中获取到完整的 `User` 对象，并将其 `roles` 属性（例如 "ROLE_USER,ROLE_ADMIN"）放入
     `claims` Map，然后传递给 `JwtUtil`。

4. `JwtUtil.generateToken()`:

   - 根据用户 ID (`subject`) 和传递过来的 `claims` (包含 `roles` 等信息) 生成 JWT Token。
   
   - 使用预定义的 `SECRET_KEY` 对 Token 进行签名，确保其完整性和真实性。
   
   - 将生成的紧凑型 JWT 字符串返回。

5. 返回 Token: `UserServiceImpl.login()` 将 JWT Token 返回给 `UserController`，最终 `UserController` 将 Token 返回给客户端。客户端应将此
Token 保存，用于后续访问受保护资源。

### 2. 资源访问 (授权) 流程
当客户端携带 JWT Token 访问受保护的 API 接口时，流程如下：

1. 携带 Token 访问: 客户端在 `Authorization` 请求头中携带 `Bearer [JWT Token]` 访问受保护的 API (例如 `GET /api/user/{userId}` )。

2. Spring Security 过滤器链拦截: 请求首先被 Spring Security 的过滤器链拦截。

3. `JwtAuthenticationFilter` 介入 (你自定义的过滤器):

   - 这个过滤器在 `SecurityConfig` 中被 `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` 添加，确保它在
   Spring Security 的默认认证过滤器之前执行。
   
   - 它从请求头中提取 JWT Token。
   
   - 关键点： 调用 `JwtUtil.parseToken(token)` 获取用户 ID，并调用 `JwtUtil.getRolesFromToken(token)` 直接从 Token 的 Claims
   中解析出用户的角色列表（例如 `["ROLE_USER", "ROLE_ADMIN"]`）。
   
   - 虽然权限信息已在 Token 中，但为了健壮性，通常还会调用 `userService.getUserById(userId)` 从数据库加载用户，验证用户的状态（是否被禁用、锁定等）。
   
   - 将解析出的用户主体 (`User` 对象) 和从 Token 中获得的权限（`authorities` 集合）构建成一个 UsernamePasswordAuthenticationToken
   对象。
   
   - 关键点： 调用 `SecurityContextHolder.getContext().setAuthentication(authentication)` 将这个认证对象设置到 Spring Security
   的上下文中。这意味着当前请求的线程现在拥有了该用户的身份和权限信息。
   
   - 最后，过滤器将请求传递给过滤器链中的下一个过滤器 (filterChain.doFilter(request, response))。

4. `@EnableMethodSecurity` 和 `@PreAuthorize` 检查:

   - 由于你在 SecurityConfig 中添加了 `@EnableMethodSecurity` 注解，Spring Security 会在方法执行前检查方法上的安全注解。
   
   - 当请求到达 `UserController.getUserById()` 方法时，`@PreAuthorize("hasRole('ADMIN')`") 注解会激活。
   
   - Spring Security 从 `SecurityContextHolder` 中获取当前请求的 `Authentication` 对象，并检查其 `authorities` 集合是否包含
   `ROLE_ADMIN` 权限。
   
   - 如果包含 `ROLE_ADMIN`，则允许方法执行。

   - 如果不包含 `ROLE_ADMIN`，则拒绝访问，通常会抛出 `AccessDeniedException` (HTTP 403 Forbidden)。

5. 方法执行或拒绝： 根据授权结果，`getUserById` 方法要么被执行并返回数据，要么被拒绝访问。

## 总结
你的项目通过 JWT 实现了 Spring Security 的认证和授权分离。

- 认证阶段 (`/login`) 负责验证用户凭据并生成包含用户 ID 和角色信息的 JWT。

- 授权阶段 (`JwtAuthenticationFilter` 和 `@PreAuthorize`) 负责解析 JWT，将用户身份和权限（从 JWT 中提取）放入
`SecurityContextHolder`，然后 `@PreAuthorize` 注解基于此上下文中的权限进行细粒度的访问控制。

关键在于确保 JWT Token 在生成时就包含了正确的角色信息，并且 `JwtAuthenticationFilter` 能够正确地从 Token 中提取这些信息并设置到
`SecurityContextHolder` 中，这样 `@PreAuthorize` 才能正确地进行判断。通过我们之前的修改，现在这些流程应该能够协同工作了。