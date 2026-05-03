package com.notebook.dto;

/**
 * 更新用户设置请求参数
 *
 * @author notebook
 */
public class UpdateSettingsDTO {

    private String theme;

    private Integer fontSize;

    private String fontFamily;

    private Integer editorMode;

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
}
