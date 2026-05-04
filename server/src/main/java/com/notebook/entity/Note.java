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
     * 笔记内容（Markdown格式）
     */
    private String content;

    /**
     * Markdown原始内容（用于编辑）
     */
    private String mdContent;

    /**
     * 编译后的HTML内容（用于预览）
     */
    private String htmlContent;

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
     * 是否公开分享（0否，1是）
     */
    private Integer isPublic;

    /**
     * 分享访问码
     */
    private String shareCode;

    /**
     * 分享过期时间
     */
    private LocalDateTime shareExpireTime;

    /**
     * 分享浏览次数
     */
    private Integer shareViewCount;

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

    public String getMdContent() {
        return mdContent;
    }

    public void setMdContent(String mdContent) {
        this.mdContent = mdContent;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
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

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public LocalDateTime getShareExpireTime() {
        return shareExpireTime;
    }

    public void setShareExpireTime(LocalDateTime shareExpireTime) {
        this.shareExpireTime = shareExpireTime;
    }

    public Integer getShareViewCount() {
        return shareViewCount;
    }

    public void setShareViewCount(Integer shareViewCount) {
        this.shareViewCount = shareViewCount;
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
