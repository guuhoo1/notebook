package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类数据访问接口
 * 提供分类表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
