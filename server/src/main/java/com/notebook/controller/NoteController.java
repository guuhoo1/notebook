package com.notebook.controller;

import com.notebook.common.PageQuery;
import com.notebook.common.PageResult;
import com.notebook.common.R;
import com.notebook.dto.NoteArchiveDTO;
import com.notebook.dto.NoteDTO;
import com.notebook.dto.NoteMoveDTO;
import com.notebook.dto.NotePinDTO;
import com.notebook.dto.NoteShareDTO;
import com.notebook.entity.Note;
import com.notebook.entity.Tag;
import com.notebook.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/list")
    public R<PageResult<Note>> getList(PageQuery query) {
        return noteService.getList(query);
    }

    @GetMapping("/{id}")
    public R<Note> getDetail(@PathVariable Long id) {
        return noteService.getDetail(id);
    }

    @PostMapping
    public R<Note> create(@RequestBody NoteDTO dto) {
        return noteService.create(dto.getTitle(), dto.getContent(), dto.getMdContent(), 
                dto.getHtmlContent(), dto.getSummary(), dto.getCategoryId(), dto.getTagIds(), dto.getStatus());
    }

    @PutMapping("/{id}")
    public R<Note> update(@PathVariable Long id, @RequestBody NoteDTO dto) {
        return noteService.update(id, dto.getTitle(), dto.getContent(), dto.getMdContent(),
                dto.getHtmlContent(), dto.getSummary(), dto.getCategoryId(), dto.getTagIds(), dto.getStatus());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return noteService.delete(id);
    }

    @PutMapping("/{id}/pin")
    public R<Void> pin(@PathVariable Long id, @RequestBody NotePinDTO dto) {
        return noteService.pin(id, dto.getIsPinned());
    }

    @PutMapping("/{id}/archive")
    public R<Void> archive(@PathVariable Long id, @RequestBody NoteArchiveDTO dto) {
        return noteService.archive(id, dto.getStatus());
    }

    @PutMapping("/{id}/move")
    public R<Void> move(@PathVariable Long id, @RequestBody NoteMoveDTO dto) {
        return noteService.move(id, dto.getCategoryId());
    }

    @GetMapping("/{id}/tags")
    public R<List<Tag>> getNoteTags(@PathVariable Long id) {
        return noteService.getNoteTags(id);
    }

    /**
     * 设置笔记分享状态
     *
     * @param id 笔记ID
     * @param dto 分享设置
     * @return 分享结果
     */
    @PostMapping("/{id}/share")
    public R<Note> setShare(@PathVariable Long id, @RequestBody NoteShareDTO dto) {
        return noteService.setShare(id, dto.getIsPublic());
    }

    /**
     * 取消笔记分享
     *
     * @param id 笔记ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/share")
    public R<Void> cancelShare(@PathVariable Long id) {
        return noteService.cancelShare(id);
    }

    /**
     * 获取分享链接信息
     *
     * @param id 笔记ID
     * @return 分享链接信息
     */
    @GetMapping("/{id}/share")
    public R<java.util.Map<String, Object>> getShareInfo(@PathVariable Long id) {
        return noteService.getShareInfo(id);
    }
}
