package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT * FROM note WHERE user_id = #{userId} AND deleted = 0 " +
            "AND (MATCH(title, summary) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "OR title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR summary LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY is_pinned DESC, update_time DESC LIMIT 20")
    List<Note> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("SELECT * FROM note WHERE user_id = #{userId} AND deleted = 1 ORDER BY deleted_at DESC")
    List<Note> selectDeletedNotes(@Param("userId") Long userId);

    @Update("UPDATE note SET deleted = 0, deleted_at = NULL WHERE id = #{id} AND user_id = #{userId}")
    int restoreNote(@Param("id") Long id, @Param("userId") Long userId);

    @Update("DELETE FROM note WHERE id = #{id} AND user_id = #{userId}")
    int permanentDelete(@Param("id") Long id, @Param("userId") Long userId);
}
