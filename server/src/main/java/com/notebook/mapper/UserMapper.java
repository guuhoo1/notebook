package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 * 提供用户表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
