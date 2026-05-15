package com.notebook.dto;

import com.notebook.entity.Note;

/**
 * 分享笔记预览响应 DTO
 * 包含笔记和分享人信息
 *
 * @author notebook
 */
public class ShareNoteVO {

    /**
     * 笔记信息
     */
    private Note note;

    /**
     * 分享人昵称
     */
    private String authorName;

    /**
     * 分享人头像
     */
    private String authorAvatar;

    public ShareNoteVO() {
    }

    public ShareNoteVO(Note note, String authorName, String authorAvatar) {
        this.note = note;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }
}
