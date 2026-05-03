package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签数据访问接口
 * 提供标签表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
