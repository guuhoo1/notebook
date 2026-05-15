package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notebook.common.R;
import com.notebook.entity.Note;
import com.notebook.entity.NoteVersion;
import com.notebook.mapper.NoteMapper;
import com.notebook.mapper.NoteVersionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteVersionService {

    private final NoteVersionMapper noteVersionMapper;
    private final NoteMapper noteMapper;

    private static final int MAX_VERSIONS = 50;

    public NoteVersionService(NoteVersionMapper noteVersionMapper, NoteMapper noteMapper) {
        this.noteVersionMapper = noteVersionMapper;
        this.noteMapper = noteMapper;
    }

    @Transactional
    public void createVersion(Long noteId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return;
        }

        Integer maxVersion = noteVersionMapper.selectMaxVersionNumber(noteId);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        NoteVersion version = new NoteVersion();
        version.setNoteId(noteId);
        version.setUserId(userId);
        version.setVersionNumber(nextVersion);
        version.setTitle(note.getTitle());
        version.setContent(note.getContent());
        version.setMdContent(note.getMdContent());
        version.setHtmlContent(note.getHtmlContent());
        version.setSummary(note.getSummary());
        version.setCreatedAt(LocalDateTime.now());

        noteVersionMapper.insert(version);

        cleanupOldVersions(noteId, userId);
    }

    private void cleanupOldVersions(Long noteId, Long userId) {
        List<NoteVersion> versions = noteVersionMapper.selectByNoteId(noteId, userId);
        
        if (versions.size() > MAX_VERSIONS) {
            int toRemove = versions.size() - MAX_VERSIONS;
            for (int i = versions.size() - 1; i >= versions.size() - toRemove; i--) {
                noteVersionMapper.deleteById(versions.get(i).getId());
            }
        }
    }

    public R<List<NoteVersion>> getVersions(Long noteId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        List<NoteVersion> versions = noteVersionMapper.selectByNoteId(noteId, userId);
        return R.ok(versions);
    }

    public R<NoteVersion> getVersion(Long noteId, Integer versionNumber) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        List<NoteVersion> versions = noteVersionMapper.selectByNoteId(noteId, userId);
        NoteVersion found = versions.stream()
                .filter(v -> v.getVersionNumber().equals(versionNumber))
                .findFirst()
                .orElse(null);

        if (found == null) {
            return R.fail("版本不存在");
        }

        return R.ok(found);
    }

    @Transactional
    public R<Void> restoreVersion(Long noteId, Integer versionNumber) {
        Long userId = StpUtil.getLoginIdAsLong();

        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            return R.fail("笔记不存在");
        }

        List<NoteVersion> versions = noteVersionMapper.selectByNoteId(noteId, userId);
        NoteVersion versionToRestore = versions.stream()
                .filter(v -> v.getVersionNumber().equals(versionNumber))
                .findFirst()
                .orElse(null);

        if (versionToRestore == null) {
            return R.fail("版本不存在");
        }

        note.setTitle(versionToRestore.getTitle());
        note.setContent(versionToRestore.getContent());
        note.setMdContent(versionToRestore.getMdContent());
        note.setHtmlContent(versionToRestore.getHtmlContent());
        note.setSummary(versionToRestore.getSummary());

        noteMapper.updateById(note);

        createVersion(noteId);

        return R.<Void>ok().message("恢复成功");
    }
}
