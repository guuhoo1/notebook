package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.notebook.common.R;
import com.notebook.dto.ShareHistoryVO;
import com.notebook.entity.Note;
import com.notebook.entity.ShareHistory;
import com.notebook.entity.User;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.ShareHistoryMapper;
import com.notebook.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分享浏览历史 Service
 */
@Service
public class ShareHistoryService {

    private final ShareHistoryMapper shareHistoryMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;

    public ShareHistoryService(ShareHistoryMapper shareHistoryMapper, NoteMapper noteMapper, UserMapper userMapper) {
        this.shareHistoryMapper = shareHistoryMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
    }

    /**
     * 保存浏览记录
     */
    public void saveHistory(Long noteId, String shareCode, String ip) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户也记录，userId留空
        }

        // 检查是否已有相同记录（同一用户/IP，同一笔记，最近24小时内）
        LambdaQueryWrapper<ShareHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ShareHistory::getShareCode, shareCode);
        if (userId != null) {
            wrapper.eq(ShareHistory::getUserId, userId);
        } else if (ip != null) {
            wrapper.eq(ShareHistory::getVisitIp, ip);
        }
        wrapper.ge(ShareHistory::getCreateTime, LocalDateTime.now().minusDays(1));
        
        ShareHistory existing = shareHistoryMapper.selectOne(wrapper);
        if (existing != null) {
            // 24小时内同一人访问同一笔记，只更新最后访问时间
            existing.setUpdateTime(LocalDateTime.now());
            shareHistoryMapper.updateById(existing);
            return;
        }

        ShareHistory history = new ShareHistory();
        history.setNoteId(noteId);
        history.setShareCode(shareCode);
        history.setUserId(userId);
        history.setVisitIp(ip);
        shareHistoryMapper.insert(history);
    }

    /**
     * 获取当前用户的浏览历史
     */
    public R<List<ShareHistoryVO>> getMyHistory(Integer limit) {
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return R.fail("请先登录");
        }

        LambdaQueryWrapper<ShareHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ShareHistory::getUserId, userId);
        wrapper.orderByDesc(ShareHistory::getUpdateTime);
        wrapper.last(limit != null ? "LIMIT " + limit : "LIMIT 20");

        List<ShareHistory> histories = shareHistoryMapper.selectList(wrapper);
        
        List<ShareHistoryVO> result = new ArrayList<>();
        for (ShareHistory history : histories) {
            Note note = noteMapper.selectById(history.getNoteId());
            if (note == null) continue;

            User author = userMapper.selectById(note.getUserId());
            
            ShareHistoryVO vo = new ShareHistoryVO();
            vo.setId(history.getId());
            vo.setNoteId(history.getNoteId());
            vo.setShareCode(history.getShareCode());
            vo.setTitle(note.getTitle());
            vo.setAuthorId(note.getUserId());
            vo.setAuthorName(author != null ? author.getNickname() : "匿名用户");
            vo.setAuthorAvatar(author != null ? author.getAvatar() : null);
            vo.setShareViewCount(note.getShareViewCount());
            vo.setVisitTime(history.getUpdateTime());
            vo.setCreateTime(history.getCreateTime());
            
            result.add(vo);
        }

        return R.ok(result);
    }

    /**
     * 清空我的浏览历史
     */
    public R<Void> clearMyHistory() {
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return R.fail("请先登录");
        }

        LambdaQueryWrapper<ShareHistory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ShareHistory::getUserId, userId);
        shareHistoryMapper.delete(wrapper);
        
        return R.ok();
    }
}
