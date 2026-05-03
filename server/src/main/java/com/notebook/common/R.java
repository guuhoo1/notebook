package com.notebook.common;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 用于封装所有API接口的返回数据
 *
 * @param <T> 响应数据类型
 * @author notebook
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200-成功，其他-失败
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public R() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回成功结果（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 返回成功结果（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /**
     * 返回成功结果（带消息和数据）
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 返回失败结果（默认消息）
     *
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    /**
     * 返回失败结果（带消息）
     *
     * @param msg 响应消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    /**
     * 返回失败结果（带状态码和消息）
     *
     * @param code 状态码
     * @param msg  响应消息
     * @param <T>  数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /**
     * 设置响应消息（链式调用）
     *
     * @param msg 响应消息
     * @return 当前响应对象
     */
    public R<T> message(String msg) {
        this.setMsg(msg);
        return this;
    }

    /**
     * 设置状态码（链式调用）
     *
     * @param code 状态码
     * @return 当前响应对象
     */
    public R<T> code(int code) {
        this.setCode(code);
        return this;
    }
}
