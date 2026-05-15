package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.entity.NoteVersion;
import com.notebook.service.NoteVersionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note/{noteId}/versions")
public class NoteVersionController {

    private final NoteVersionService noteVersionService;

    public NoteVersionController(NoteVersionService noteVersionService) {
        this.noteVersionService = noteVersionService;
    }

    @GetMapping
    public R<List<NoteVersion>> getVersions(@PathVariable Long noteId) {
        return noteVersionService.getVersions(noteId);
    }

    @GetMapping("/{versionNumber}")
    public R<NoteVersion> getVersion(@PathVariable Long noteId, @PathVariable Integer versionNumber) {
        return noteVersionService.getVersion(noteId, versionNumber);
    }

    @PutMapping("/{versionNumber}/restore")
    public R<Void> restoreVersion(@PathVariable Long noteId, @PathVariable Integer versionNumber) {
        return noteVersionService.restoreVersion(noteId, versionNumber);
    }
}
