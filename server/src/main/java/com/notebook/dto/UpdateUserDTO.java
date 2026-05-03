package com.notebook.dto;

/**
 * 更新用户信息请求参数
 *
 * @author notebook
 */
public class UpdateUserDTO {

    private String nickname;

    private Integer gender;

    private String avatar;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
