package com.notebook.controller;

import com.notebook.common.R;
import com.notebook.dto.CategoryDTO;
import com.notebook.dto.CategorySortDTO;
import com.notebook.entity.Category;
import com.notebook.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public R<List<Category>> getList() {
        return categoryService.getList();
    }

    @PostMapping
    public R<Category> create(@RequestBody CategoryDTO dto) {
        return categoryService.create(dto.getName(), dto.getDescription(), dto.getColor(), dto.getIcon());
    }

    @PutMapping("/{id}")
    public R<Category> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        return categoryService.update(id, dto.getName(), dto.getDescription(), dto.getColor(), dto.getIcon());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }

    @PutMapping("/sort")
    public R<Void> sort(@RequestBody CategorySortDTO dto) {
        return categoryService.sort(dto);
    }
}
