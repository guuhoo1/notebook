package com.notebook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notebook.entity.User;
import com.notebook.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 开发工具控制器
 * 仅用于开发环境，生产环境应删除
 *
 * @author notebook
 */
@RestController
@RequestMapping("/dev")
public class DevController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper;

    public DevController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 生成BCrypt密码哈希
     *
     * @param password 明文密码
     * @return 密码哈希
     */
    @GetMapping("/encodePassword")
    public String encodePassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return "Raw: " + password + "\nHash: " + hash + "\nLength: " + hash.length();
    }

    /**
     * 重置测试用户密码
     * 将 test@example.com 用户的密码重置为指定密码
     *
     * @param newPassword 新密码
     * @return 操作结果
     */
    @PostMapping("/resetTestPassword")
    public String resetTestPassword(@RequestParam(defaultValue = "password123") String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, "test@example.com");
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return "测试用户不存在，请先注册";
        }

        String hash = passwordEncoder.encode(newPassword);
        user.setPassword(hash);
        userMapper.updateById(user);

        return "密码已重置!\n邮箱: test@example.com\n密码: " + newPassword + "\n哈希: " + hash;
    }
}
