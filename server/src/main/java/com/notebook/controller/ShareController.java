package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.entity.Note;
import com.notebook.service.NoteService;
import org.springframework.web.bind.annotation.*;

/**
 * 分享预览控制器
 * 处理未登录用户通过分享链接访问笔记的请求
 *
 * @author notebook
 */
@RestController
@RequestMapping("/share")
public class ShareController {

    private final NoteService noteService;

    public ShareController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 通过分享码访问笔记
     *
     * @param shareCode 分享访问码
     * @return 笔记详情
     */
    @GetMapping("/{shareCode}")
    public R<Note> viewShare(@PathVariable String shareCode) {
        return noteService.viewShare(shareCode);
    }
}
