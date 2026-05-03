package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notebook.common.R;
import com.notebook.entity.Category;
import com.notebook.entity.Note;
import com.notebook.entity.Tag;
import com.notebook.mapper.CategoryMapper;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.TagMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final NoteMapper noteMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public SearchService(NoteMapper noteMapper, CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.noteMapper = noteMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    public R<List<Note>> searchNotes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String searchKeyword = keyword.trim();

        List<Note> notes = noteMapper.searchByKeyword(userId, searchKeyword);

        return R.ok(notes);
    }

    public R<List<Object>> searchAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        Long userId = StpUtil.getLoginIdAsLong();
        String searchKeyword = keyword.trim();

        List<Object> results = new ArrayList<>();

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId)
                .and(w -> w
                        .like(Note::getTitle, searchKeyword)
                        .or()
                        .like(Note::getSummary, searchKeyword)
                )
                .in(Note::getStatus, 0, 1)
                .orderByDesc(Note::getUpdateTime)
                .last("LIMIT 10");
        List<Note> notes = noteMapper.selectList(noteWrapper);
        results.addAll(notes);

        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getUserId, userId)
                .like(Category::getName, searchKeyword)
                .orderByDesc(Category::getCreateTime)
                .last("LIMIT 5");
        List<Category> categories = categoryMapper.selectList(categoryWrapper);
        results.addAll(categories);

        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId)
                .like(Tag::getName, searchKeyword)
                .orderByDesc(Tag::getCreateTime)
                .last("LIMIT 5");
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        results.addAll(tags);

        return R.ok(results);
    }
}
