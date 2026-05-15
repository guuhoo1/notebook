package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.NoteVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteVersionMapper extends BaseMapper<NoteVersion> {

    @Select("SELECT * FROM note_version WHERE note_id = #{noteId} AND user_id = #{userId} ORDER BY version_number DESC")
    List<NoteVersion> selectByNoteId(@Param("noteId") Long noteId, @Param("userId") Long userId);

    @Select("SELECT MAX(version_number) FROM note_version WHERE note_id = #{noteId}")
    Integer selectMaxVersionNumber(@Param("noteId") Long noteId);
}
