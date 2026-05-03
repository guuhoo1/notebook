package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.entity.Note;
import com.notebook.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/notes")
    public R<List<Note>> searchNotes(@RequestParam String keyword) {
        return searchService.searchNotes(keyword);
    }

    @GetMapping("/all")
    public R<List<Object>> searchAll(@RequestParam String keyword) {
        return searchService.searchAll(keyword);
    }
}
