package com.notebook.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置实体类
 * 存储用户偏好设置信息
 *
 * @author notebook
 */
@TableName("user_settings")
public class UserSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 主题（light/dark）
     */
    private String theme;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 字体类型
     */
    private String fontFamily;

    /**
     * 编辑器模式（0普通，1沉浸）
     */
    private Integer editorMode;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public UserSettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getEditorMode() {
        return editorMode;
    }

    public void setEditorMode(Integer editorMode) {
        this.editorMode = editorMode;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
