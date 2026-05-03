package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.UpdateSettingsDTO;
import com.notebook.entity.UserSettings;
import com.notebook.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 设置控制器
 * 处理用户设置相关请求
 *
 * @author notebook
 */
@RestController
@RequestMapping
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户设置
     *
     * @return 用户设置
     */
    @GetMapping("/settings")
    public R<UserSettings> getSettings() {
        return userService.getSettings();
    }

    /**
     * 更新用户设置
     *
     * @param dto 更新参数
     * @return 更新结果
     */
    @PutMapping("/settings")
    public R<Void> updateSettings(@RequestBody UpdateSettingsDTO dto) {
        return userService.updateSettings(dto.getTheme(), dto.getFontSize(), dto.getFontFamily(), dto.getEditorMode());
    }
}
