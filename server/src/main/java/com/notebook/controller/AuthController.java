package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.LoginDTO;
import com.notebook.dto.RegisterDTO;
import com.notebook.entity.User;
import com.notebook.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等请求
 *
 * @author notebook
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        return authService.register(dto.getEmail(), dto.getPhone(), dto.getPassword(), dto.getNickname());
    }

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return authService.login(dto.getAccount(), dto.getPassword());
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        return authService.logout();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/userInfo")
    public R<User> getUserInfo() {
        return authService.getUserInfo();
    }
}
