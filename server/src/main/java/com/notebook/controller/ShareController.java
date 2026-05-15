package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.ShareHistoryVO;
import com.notebook.dto.ShareNoteVO;
import com.notebook.service.NoteService;
import com.notebook.service.ShareHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final ShareHistoryService shareHistoryService;

    public ShareController(NoteService noteService, ShareHistoryService shareHistoryService) {
        this.noteService = noteService;
        this.shareHistoryService = shareHistoryService;
    }

    /**
     * 通过分享码访问笔记
     *
     * @param shareCode 分享访问码
     * @return 笔记详情（含分享人信息）
     */
    @GetMapping("/{shareCode}")
    public R<ShareNoteVO> viewShare(@PathVariable String shareCode, HttpServletRequest request) {
        R<ShareNoteVO> result = noteService.viewShare(shareCode);
        
        // 成功获取笔记时保存浏览记录
        if (result.getCode() == 200 && result.getData() != null && result.getData().getNote() != null) {
            String ip = getClientIp(request);
            shareHistoryService.saveHistory(
                result.getData().getNote().getId(),
                shareCode,
                ip
            );
        }
        
        return result;
    }

    /**
     * 获取当前用户的分享浏览历史
     *
     * @param limit 返回记录数限制，默认20条
     * @return 浏览历史列表
     */
    @GetMapping("/history")
    public R<List<ShareHistoryVO>> getMyHistory(@RequestParam(required = false) Integer limit) {
        return shareHistoryService.getMyHistory(limit);
    }

    /**
     * 清空当前用户的分享浏览历史
     *
     * @return 操作结果
     */
    @DeleteMapping("/history")
    public R<Void> clearMyHistory() {
        return shareHistoryService.clearMyHistory();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
