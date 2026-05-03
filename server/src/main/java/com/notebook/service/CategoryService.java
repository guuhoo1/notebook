package com.notebook.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.notebook.common.R;
import com.notebook.dto.CategorySortDTO;
import com.notebook.entity.Category;
import com.notebook.mapper.CategoryMapper;
import com.notebook.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final NoteMapper noteMapper;

    public CategoryService(CategoryMapper categoryMapper, NoteMapper noteMapper) {
        this.categoryMapper = categoryMapper;
        this.noteMapper = noteMapper;
    }

    public R<List<Category>> getList() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .orderByAsc(Category::getSortOrder)
                .orderByDesc(Category::getCreateTime);
        
        List<Category> categories = categoryMapper.selectList(wrapper);
        return R.ok(categories);
    }

    public R<Category> create(String name, String description, String color, String icon) {
        if (name == null || name.trim().isEmpty()) {
            return R.fail("分类名称不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, name.trim());
        if (categoryMapper.selectCount(wrapper) > 0) {
            return R.fail("分类名称已存在");
        }

        LambdaQueryWrapper<Category> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Category::getUserId, userId);
        Long maxSortOrder = categoryMapper.selectCount(countWrapper);

        Category category = new Category();
        category.setUserId(userId);
        category.setName(name.trim());
        category.setDescription(description);
        category.setColor(color != null ? color : "#181d26");
        category.setIcon(icon);
        category.setSortOrder(maxSortOrder.intValue());
        category.setNoteCount(0);

        categoryMapper.insert(category);
        return R.ok(category).message("创建成功");
    }

    public R<Category> update(Long id, String name, String description, String color, String icon) {
        Long userId = StpUtil.getLoginIdAsLong();

        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            return R.fail("分类不存在");
        }

        if (name != null && !name.trim().isEmpty()) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getUserId, userId)
                    .eq(Category::getName, name.trim())
                    .ne(Category::getId, id);
            if (categoryMapper.selectCount(wrapper) > 0) {
                return R.fail("分类名称已存在");
            }
            category.setName(name.trim());
        }

        if (description != null) {
            category.setDescription(description);
        }
        if (color != null) {
            category.setColor(color);
        }
        if (icon != null) {
            category.setIcon(icon);
        }

        categoryMapper.updateById(category);
        return R.ok(category).message("更新成功");
    }

    @Transactional
    public R<Void> delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            return R.fail("分类不存在");
        }

        categoryMapper.deleteById(id);

        return R.<Void>ok().message("删除成功");
    }

    @Transactional
    public R<Void> sort(CategorySortDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (dto.getOrders() == null || dto.getOrders().isEmpty()) {
            return R.fail("排序数据不能为空");
        }

        for (CategorySortDTO.CategorySortItem item : dto.getOrders()) {
            Category category = categoryMapper.selectById(item.getId());
            if (category != null && category.getUserId().equals(userId)) {
                LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Category::getId, item.getId())
                        .set(Category::getSortOrder, item.getSortOrder());
                categoryMapper.update(null, updateWrapper);
            }
        }

        return R.<Void>ok().message("排序成功");
    }
}
