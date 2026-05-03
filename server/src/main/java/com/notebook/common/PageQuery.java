package com.notebook.common;

import java.io.Serializable;

/**
 * 分页查询参数类
 * 用于接收前端传递的分页参数
 *
 * @author notebook
 */
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认第1页
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10条
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（asc/desc）
     */
    private String sort;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 状态筛选
     */
    private Integer status;

    /**
     * 分类ID筛选
     */
    private Long categoryId;

    public PageQuery() {
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 计算偏移量
     * 用于SQL的LIMIT语句
     *
     * @return 偏移量
     */
    public Integer getOffset() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        return (pageNum - 1) * pageSize;
    }
}
