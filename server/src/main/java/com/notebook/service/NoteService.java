package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.notebook.common.PageQuery;
import com.notebook.common.PageResult;
import com.notebook.common.R;
import com.notebook.dto.ShareNoteVO;
import com.notebook.entity.Note;
import com.notebook.entity.NoteTag;
import com.notebook.entity.Tag;
import com.notebook.entity.User;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.NoteTagMapper;
import com.notebook.mapper.TagMapper;
import com.notebook.mapper.UserMapper;
import com.notebook.service.NoteVersionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteMapper noteMapper;
    private final NoteTagMapper noteTagMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final NoteVersionService noteVersionService;

    public NoteService(NoteMapper noteMapper, NoteTagMapper noteTagMapper, TagMapper tagMapper, UserMapper userMapper, @Lazy NoteVersionService noteVersionService) {
        this.noteMapper = noteMapper;
        this.noteTagMapper = noteTagMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.noteVersionService = noteVersionService;
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
    public R<Note> create(String title, String content, String mdContent, String htmlContent, String summary, Long categoryId, List<Long> tagIds, Integer status) {
        if (title == null || title.trim().isEmpty()) {
            return R.fail("标题不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(title.trim());
        note.setContent(content);
        note.setMdContent(mdContent);
        note.setHtmlContent(htmlContent);
        note.setSummary(summary != null ? summary : extractSummary(htmlContent != null ? htmlContent : content));
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
    public R<Note> update(Long id, String title, String content, String mdContent, String htmlContent, String summary, Long categoryId, List<Long> tagIds, Integer status) {
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
        if (mdContent != null) {
            note.setMdContent(mdContent);
        }
        if (htmlContent != null) {
            note.setHtmlContent(htmlContent);
        }
        if (summary != null) {
            note.setSummary(summary);
        } else if (htmlContent != null) {
            note.setSummary(extractSummary(htmlContent));
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

        noteVersionService.createVersion(id);

        return R.ok(note).message("更新成功");
    }

    @Transactional
    public R<Void> delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        // 设置删除时间
        note.setDeletedAt(LocalDateTime.now());
        noteMapper.updateById(note);

        // 逻辑删除
        noteMapper.deleteById(id);

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, id);
        noteTagMapper.delete(wrapper);

        return R.<Void>ok().message("删除成功");
    }

    public R<List<Note>> getDeletedNotes() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Note> notes = noteMapper.selectDeletedNotes(userId);
        return R.ok(notes);
    }

    @Transactional
    public R<Void> restoreNote(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        int count = noteMapper.restoreNote(id, userId);
        if (count == 0) {
            return R.fail("笔记不存在或无法恢复");
        }

        return R.<Void>ok().message("恢复成功");
    }

    @Transactional
    public R<Void> permanentDelete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 先删除关联的标签
        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, id);
        noteTagMapper.delete(wrapper);

        // 永久删除
        int count = noteMapper.permanentDelete(id, userId);
        if (count == 0) {
            return R.fail("笔记不存在或无法删除");
        }

        return R.<Void>ok().message("永久删除成功");
    }

    @Transactional
    public R<Void> emptyRecycleBin() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Note> deletedNotes = noteMapper.selectDeletedNotes(userId);

        for (Note note : deletedNotes) {
            // 删除关联的标签
            LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NoteTag::getNoteId, note.getId());
            noteTagMapper.delete(wrapper);

            // 永久删除
            noteMapper.permanentDelete(note.getId(), userId);
        }

        return R.<Void>ok().message("回收站已清空");
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

    private static final int MAX_SHARE_COUNT = 10;
    private static final int MAX_DAILY_SHARE = 5;

    public R<Note> setShare(Long id, Integer isPublic) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        if (isPublic == 1) {
            LambdaQueryWrapper<Note> shareCountWrapper = new LambdaQueryWrapper<>();
            shareCountWrapper.eq(Note::getUserId, userId).eq(Note::getIsPublic, 1);
            long shareCount = noteMapper.selectCount(shareCountWrapper);
            if (shareCount >= MAX_SHARE_COUNT) {
                return R.fail("最多只能分享" + MAX_SHARE_COUNT + "篇笔记");
            }

            if (note.getIsPublic() == null || note.getIsPublic() != 1) {
                String shareCode = generateShareCode();
                while (isShareCodeExists(shareCode)) {
                    shareCode = generateShareCode();
                }
                note.setShareCode(shareCode);
                note.setShareExpireTime(LocalDateTime.now().plusDays(30));
                note.setShareViewCount(0);
            }
            note.setIsPublic(1);
        } else {
            note.setIsPublic(0);
        }

        noteMapper.updateById(note);
        return R.ok(note).message(isPublic == 1 ? "分享成功" : "已取消分享");
    }

    public R<Void> cancelShare(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        note.setIsPublic(0);
        note.setShareCode(null);
        note.setShareExpireTime(null);
        noteMapper.updateById(note);

        return R.<Void>ok().message("已取消分享");
    }

    public R<Map<String, Object>> getShareInfo(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("isPublic", note.getIsPublic());
        info.put("shareCode", note.getShareCode());
        info.put("shareUrl", note.getIsPublic() == 1 && note.getShareCode() != null 
                ? "/share/" + note.getShareCode() 
                : null);
        info.put("shareExpireTime", note.getShareExpireTime());
        info.put("shareViewCount", note.getShareViewCount());

        return R.ok(info);
    }

    public R<ShareNoteVO> viewShare(String shareCode) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShareCode, shareCode)
                .eq(Note::getIsPublic, 1)
                .eq(Note::getStatus, 1)
                .eq(Note::getDeleted, 0);

        Note note = noteMapper.selectOne(wrapper);

        if (note == null) {
            return R.fail("分享链接无效或已过期");
        }

        if (note.getShareExpireTime() != null && note.getShareExpireTime().isBefore(LocalDateTime.now())) {
            note.setIsPublic(0);
            note.setShareCode(null);
            note.setShareExpireTime(null);
            noteMapper.updateById(note);
            return R.fail("分享链接已过期");
        }

        note.setShareViewCount(note.getShareViewCount() != null ? note.getShareViewCount() + 1 : 1);
        noteMapper.updateById(note);

        User author = userMapper.selectById(note.getUserId());
        String authorName = author != null ? author.getNickname() : "匿名用户";
        String authorAvatar = author != null ? author.getAvatar() : null;

        ShareNoteVO vo = new ShareNoteVO(note, authorName, authorAvatar);
        return R.ok(vo);
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private boolean isShareCodeExists(String shareCode) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShareCode, shareCode);
        return noteMapper.selectCount(wrapper) > 0;
    }
}
