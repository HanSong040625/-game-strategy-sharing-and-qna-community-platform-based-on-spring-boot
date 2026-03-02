package com.huadetec.gamecommunity.config;

import com.huadetec.gamecommunity.entity.Admin;
import com.huadetec.gamecommunity.entity.User;
import com.huadetec.gamecommunity.service.AdminService;
import com.huadetec.gamecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity  // 启用 Spring Security 安全控制
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 注入 UserService 用于查询用户
    @Autowired
    private UserService userService;
    
    // 注入 AdminService 用于查询管理员
    @Autowired
    private AdminService adminService;

    // 注入密码加密工具（全局可用）
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCryptPasswordEncoder进行密码加密
        return new BCryptPasswordEncoder();
    }

    // 配置用户认证逻辑
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
            .passwordEncoder(passwordEncoder());
    }

    // 自定义 UserDetailsService：Spring Security 登录时会调用此方法查用户
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return username -> {
            // 首先在admin_user表中查找管理员用户
            Optional<Admin> adminOptional = adminService.findByUsername(username);
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                // 管理员用户拥有ADMIN角色
                return new org.springframework.security.core.userdetails.User(
                        admin.getUsername(),
                        admin.getPassword(),
                        java.util.Arrays.asList(() -> "ROLE_ADMIN")
                );
            }
            
            // 如果不是管理员，则在user表中查找普通用户
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 普通用户没有特殊角色
                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        java.util.Collections.emptyList()
                );
            }
            
            // 如果两个表中都找不到用户，抛出异常
            throw new UsernameNotFoundException("用户名不存在：" + username);
        };
    }

    // 提供 AuthenticationManager Bean，供 UserController 注入使用
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 配置 HTTP 安全规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用CSRF保护，简化开发环境
        http.csrf().disable()
            // 允许跨域请求
            .cors().and()
            // 配置会话管理
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
            // 配置授权规则，进一步简化权限控制
            .authorizeRequests()
                // 允许所有用户访问的接口
                .antMatchers("/api/admin/login", "/api/admin/register", "/api/auth/login", "/api/auth/register").permitAll()
                // 允许已认证用户访问管理员接口（放宽权限控制）
                .antMatchers("/api/admin/**").authenticated()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()
            // 完全禁用表单登录和基本认证，使用自定义认证流程
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            // 配置异常处理
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    // 未认证用户访问需要认证的资源时，返回友好的JSON响应
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\",\"data\":null}");
                });
    }
    
    // 配置CORS策略
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 使用allowedOriginPatterns代替allowedOrigins，这样可以在allowCredentials=true时使用通配符
        configuration.setAllowedOriginPatterns(java.util.Arrays.asList("http://localhost:8081", "http://localhost:8080", "http://localhost:8000"));
        // 允许所有常用的HTTP方法，包括PUT和DELETE
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}