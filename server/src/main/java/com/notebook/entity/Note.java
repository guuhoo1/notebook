package com.notebook.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体类
 * 存储笔记内容和元数据信息
 *
 * @author notebook
 */
@TableName("note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容（富文本HTML）
     */
    private String content;

    /**
     * 笔记摘要（纯文本）
     */
    private String summary;

    /**
     * 状态（0草稿，1正常，2归档）
     */
    private Integer status;

    /**
     * 是否置顶（0否，1是）
     */
    private Integer isPinned;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 逻辑删除（0正常，1删除）
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Note() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
