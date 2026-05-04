package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.UpdatePasswordDTO;
import com.notebook.dto.UpdateUserDTO;
import com.notebook.entity.User;
import com.notebook.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户控制器
 * 处理用户信息管理、设置等请求
 *
 * @author notebook
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getUserStats() {
        return userService.getUserStats();
    }

    /**
     * 更新用户信息
     *
     * @param dto 更新参数
     * @return 更新结果
     */
    @PutMapping("/info")
    public R<User> updateUserInfo(@RequestBody UpdateUserDTO dto) {
        return userService.updateUserInfo(
            dto.getNickname(),
            dto.getGender(),
            dto.getAvatar(),
            dto.getEmail(),
            dto.getPhone()
        );
    }

    /**
     * 修改密码
     *
     * @param dto 密码参数
     * @return 修改结果
     */
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody UpdatePasswordDTO dto) {
        return userService.updatePassword(dto.getOldPassword(), dto.getNewPassword());
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/avatar")
    public R<User> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }
}
