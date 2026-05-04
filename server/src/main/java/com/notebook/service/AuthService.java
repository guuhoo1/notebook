package com.notebook.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notebook.common.R;
import com.notebook.entity.User;
import com.notebook.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类
 * 处理用户登录、注册、登出等认证相关业务
 *
 * @author notebook
 */
@Service
public class AuthService {

    private final UserMapper userMapper;

    /**
     * BCrypt密码编码器
     * 用于密码的加密和匹配验证
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户注册
     *
     * @param email    邮箱
     * @param phone    手机号
     * @param password 密码
     * @param nickname 昵称
     * @return 注册结果，包含用户信息和Token
     */
    public R<Map<String, Object>> register(String email, String phone, String password, String nickname) {
        if (email == null && phone == null) {
            return R.fail("邮箱或手机号至少填写一项");
        }

        if (email != null) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, email);
            if (userMapper.selectCount(emailWrapper) > 0) {
                return R.fail("邮箱已被注册");
            }
        }

        if (phone != null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, phone);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                return R.fail("手机号已被注册");
            }
        }

        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        userMapper.insert(user);

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);

        return R.ok(data).message("注册成功");
    }

    /**
     * 用户登录
     *
     * @param account  账号（邮箱或手机号）
     * @param password 密码
     * @return 登录结果，包含用户信息和Token
     */
    public R<Map<String, Object>> login(String account, String password) {
        if (account == null || account.isEmpty()) {
            return R.fail("请输入账号");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, account).or().eq(User::getPhone, account);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return R.fail("账号或密码错误");
        }

        if (user.getStatus() != 1) {
            return R.fail("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return R.fail("账号或密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);

        return R.ok(data).message("登录成功");
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    public R<Void> logout() {
        StpUtil.logout();
        return R.<Void>ok().message("退出成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public R<User> getUserInfo() {
        try {
            Long loginId = StpUtil.getLoginIdAsLong();
            User user = userMapper.selectById(loginId);
            if (user == null) {
                return R.fail("用户不存在");
            }
            user.setPassword(null);
            return R.ok(user);
        } catch (NotLoginException e) {
            return R.fail(401, "请先登录");
        }
    }

    /**
     * 对密码进行BCrypt加密
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码哈希值
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
