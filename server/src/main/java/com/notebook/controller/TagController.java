package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.TagDTO;
import com.notebook.entity.Tag;
import com.notebook.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public R<List<Tag>> getList() {
        return tagService.getList();
    }

    @PostMapping
    public R<Tag> create(@RequestBody TagDTO dto) {
        return tagService.create(dto.getName(), dto.getColor());
    }

    @PutMapping("/{id}")
    public R<Tag> update(@PathVariable Long id, @RequestBody TagDTO dto) {
        return tagService.update(id, dto.getName(), dto.getColor());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return tagService.delete(id);
    }

    @GetMapping("/{id}/noteCount")
    public R<Long> getNoteCount(@PathVariable Long id) {
        return tagService.getNoteCount(id);
    }
}
