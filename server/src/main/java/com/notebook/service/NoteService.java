package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.notebook.common.PageQuery;
import com.notebook.common.PageResult;
import com.notebook.common.R;
import com.notebook.entity.Note;
import com.notebook.entity.NoteTag;
import com.notebook.entity.Tag;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.NoteTagMapper;
import com.notebook.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoteService {

    private final NoteMapper noteMapper;
    private final NoteTagMapper noteTagMapper;
    private final TagMapper tagMapper;

    public NoteService(NoteMapper noteMapper, NoteTagMapper noteTagMapper, TagMapper tagMapper) {
        this.noteMapper = noteMapper;
        this.noteTagMapper = noteTagMapper;
        this.tagMapper = tagMapper;
    }

    public R<PageResult<Note>> getList(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);

        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Note::getTitle, query.getKeyword().trim())
                    .or()
                    .like(Note::getSummary, query.getKeyword().trim())
            );
        }

        if (query.getStatus() != null) {
            wrapper.eq(Note::getStatus, query.getStatus());
        } else {
            wrapper.in(Note::getStatus, 0, 1);
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(Note::getCategoryId, query.getCategoryId());
        }

        wrapper.orderByDesc(Note::getIsPinned)
                .orderByDesc(Note::getUpdateTime);

        Page<Note> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Note> result = noteMapper.selectPage(page, wrapper);

        PageResult<Note> pageResult = new PageResult<>();
        pageResult.setList(result.getRecords());
        pageResult.setTotal(result.getTotal());

        return R.ok(pageResult);
    }

    public R<Note> getDetail(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);

        return R.ok(note);
    }

    @Transactional
    public R<Note> create(String title, String content, String summary, Long categoryId, List<Long> tagIds, Integer status) {
        if (title == null || title.trim().isEmpty()) {
            return R.fail("标题不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(title.trim());
        note.setContent(content);
        note.setSummary(summary != null ? summary : extractSummary(content));
        note.setCategoryId(categoryId);
        note.setStatus(status != null ? status : 1);
        note.setIsPinned(0);
        note.setViewCount(0);

        noteMapper.insert(note);

        if (tagIds != null && !tagIds.isEmpty()) {
            updateNoteTags(note.getId(), tagIds, userId);
        }

        return R.ok(note).message("创建成功");
    }

    @Transactional
    public R<Note> update(Long id, String title, String content, String summary, Long categoryId, List<Long> tagIds, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        if (title != null && !title.trim().isEmpty()) {
            note.setTitle(title.trim());
        }
        if (content != null) {
            note.setContent(content);
        }
        if (summary != null) {
            note.setSummary(summary);
        } else if (content != null) {
            note.setSummary(extractSummary(content));
        }
        if (categoryId != null) {
            note.setCategoryId(categoryId);
        }
        if (status != null) {
            note.setStatus(status);
        }

        noteMapper.updateById(note);

        if (tagIds != null) {
            updateNoteTags(id, tagIds, userId);
        }

        return R.ok(note).message("更新成功");
    }

    @Transactional
    public R<Void> delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        noteMapper.deleteById(id);

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, id);
        noteTagMapper.delete(wrapper);

        return R.<Void>ok().message("删除成功");
    }

    public R<Void> pin(Long id, Integer isPinned) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        note.setIsPinned(isPinned != null && isPinned == 1 ? 1 : 0);
        noteMapper.updateById(note);

        return R.<Void>ok().message(isPinned == 1 ? "已置顶" : "已取消置顶");
    }

    public R<Void> archive(Long id, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        note.setStatus(status != null ? status : 2);
        noteMapper.updateById(note);

        return R.<Void>ok().message(status == 1 ? "已恢复" : "已归档");
    }

    public R<Void> move(Long id, Long categoryId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        note.setCategoryId(categoryId);
        noteMapper.updateById(note);

        return R.<Void>ok().message("移动成功");
    }

    public R<List<Tag>> getNoteTags(Long noteId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        LambdaQueryWrapper<NoteTag> ntWrapper = new LambdaQueryWrapper<>();
        ntWrapper.eq(NoteTag::getNoteId, noteId);
        List<NoteTag> noteTags = noteTagMapper.selectList(ntWrapper);

        List<Tag> tags = new ArrayList<>();
        for (NoteTag nt : noteTags) {
            Tag tag = tagMapper.selectById(nt.getTagId());
            if (tag != null && tag.getDeleted() == 0) {
                tags.add(tag);
            }
        }

        return R.ok(tags);
    }

    private void updateNoteTags(Long noteId, List<Long> tagIds, Long userId) {
        LambdaQueryWrapper<NoteTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(NoteTag::getNoteId, noteId);
        noteTagMapper.delete(deleteWrapper);

        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag != null && tag.getUserId().equals(userId) && tag.getDeleted() == 0) {
                NoteTag noteTag = new NoteTag();
                noteTag.setNoteId(noteId);
                noteTag.setTagId(tagId);
                noteTagMapper.insert(noteTag);
            }
        }
    }

    private String extractSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String text = content.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ");
        if (text.length() > 200) {
            return text.substring(0, 200) + "...";
        }
        return text;
    }
}
