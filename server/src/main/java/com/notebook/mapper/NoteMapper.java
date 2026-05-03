package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记数据访问接口
 * 提供笔记表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
