package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.notebook.common.PageQuery;
import com.notebook.common.PageResult;
import com.notebook.common.R;
import com.notebook.entity.Note;
import com.notebook.entity.User;
import com.notebook.entity.UserSettings;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.UserMapper;
import com.notebook.mapper.UserSettingsMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 * 处理用户信息管理、密码修改等业务
 *
 * @author notebook
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    private final UserSettingsMapper userSettingsMapper;

    private final NoteMapper noteMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, UserSettingsMapper userSettingsMapper, NoteMapper noteMapper) {
        this.userMapper = userMapper;
        this.userSettingsMapper = userSettingsMapper;
        this.noteMapper = noteMapper;
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    public R<Map<String, Object>> getUserStats() {
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId).eq(Note::getStatus, 1);
        Long noteCount = noteMapper.selectCount(noteWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("noteCount", noteCount);

        return R.ok(stats);
    }

    /**
     * 更新用户信息
     *
     * @param nickname 昵称
     * @param gender   性别
     * @param avatar   头像URL
     * @return 更新结果
     */
    public R<User> updateUserInfo(String nickname, Integer gender, String avatar) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        if (user == null) {
            return R.fail("用户不存在");
        }

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
        user.setPassword(null);

        return R.ok(user).message("更新成功");
    }

    /**
     * 修改密码
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    public R<Void> updatePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        if (user == null) {
            return R.fail("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return R.fail("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        return R.<Void>ok().message("密码修改成功");
    }

    /**
     * 获取用户设置
     *
     * @return 用户设置
     */
    public R<UserSettings> getSettings() {
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId);
        UserSettings settings = userSettingsMapper.selectOne(wrapper);

        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme("light");
            settings.setFontSize(14);
            settings.setFontFamily("default");
            settings.setEditorMode(0);
            userSettingsMapper.insert(settings);
        }

        return R.ok(settings);
    }

    /**
     * 更新用户设置
     *
     * @param theme      主题
     * @param fontSize   字体大小
     * @param fontFamily 字体类型
     * @param editorMode 编辑器模式
     * @return 更新结果
     */
    public R<Void> updateSettings(String theme, Integer fontSize, String fontFamily, Integer editorMode) {
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId);
        UserSettings settings = userSettingsMapper.selectOne(wrapper);

        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
        }

        if (theme != null) {
            settings.setTheme(theme);
        }
        if (fontSize != null) {
            settings.setFontSize(fontSize);
        }
        if (fontFamily != null) {
            settings.setFontFamily(fontFamily);
        }
        if (editorMode != null) {
            settings.setEditorMode(editorMode);
        }

        if (settings.getId() == null) {
            userSettingsMapper.insert(settings);
        } else {
            userSettingsMapper.updateById(settings);
        }

        return R.<Void>ok().message("设置更新成功");
    }
}
