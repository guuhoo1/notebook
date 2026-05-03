package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notebook.common.R;
import com.notebook.entity.NoteTag;
import com.notebook.entity.Tag;
import com.notebook.mapper.NoteTagMapper;
import com.notebook.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagMapper tagMapper;
    private final NoteTagMapper noteTagMapper;

    public TagService(TagMapper tagMapper, NoteTagMapper noteTagMapper) {
        this.tagMapper = tagMapper;
        this.noteTagMapper = noteTagMapper;
    }

    public R<List<Tag>> getList() {
        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByDesc(Tag::getCreateTime);

        List<Tag> tags = tagMapper.selectList(wrapper);
        return R.ok(tags);
    }

    public R<Tag> create(String name, String color) {
        if (name == null || name.trim().isEmpty()) {
            return R.fail("标签名称不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getName, name.trim());
        if (tagMapper.selectCount(wrapper) > 0) {
            return R.fail("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name.trim());
        tag.setColor(color != null ? color : "#1b61c9");

        tagMapper.insert(tag);
        return R.ok(tag).message("创建成功");
    }

    public R<Tag> update(Long id, String name, String color) {
        Long userId = StpUtil.getLoginIdAsLong();

        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            return R.fail("标签不存在");
        }

        if (name != null && !name.trim().isEmpty()) {
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getUserId, userId)
                    .eq(Tag::getName, name.trim())
                    .ne(Tag::getId, id);
            if (tagMapper.selectCount(wrapper) > 0) {
                return R.fail("标签名称已存在");
            }
            tag.setName(name.trim());
        }

        if (color != null) {
            tag.setColor(color);
        }

        tagMapper.updateById(tag);
        return R.ok(tag).message("更新成功");
    }

    @Transactional
    public R<Void> delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            return R.fail("标签不存在");
        }

        tagMapper.deleteById(id);

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getTagId, id);
        noteTagMapper.delete(wrapper);

        return R.<Void>ok().message("删除成功");
    }

    public R<Long> getNoteCount(Long tagId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            return R.fail("标签不存在");
        }

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getTagId, tagId);
        Long count = noteTagMapper.selectCount(wrapper);

        return R.ok(count);
    }
}
