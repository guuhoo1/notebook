package com.notebook.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 用于封装分页查询的返回数据
 *
 * @param <T> 数据类型
 * @author notebook
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * 创建分页结果（带总数）
     *
     * @param list  数据列表
     * @param total 总记录数
     * @param <T>   数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> ok(List<T> list, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        return result;
    }

    /**
     * 创建分页结果（不带总数，使用列表长度）
     *
     * @param list 数据列表
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> ok(List<T> list) {
        return ok(list, (long) list.size());
    }
}
