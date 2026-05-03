package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记标签关联数据访问接口
 * 提供笔记标签关联表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
}
