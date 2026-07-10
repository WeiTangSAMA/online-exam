package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.online.exam.common.BusinessException;
import com.online.exam.entity.Category;
import com.online.exam.entity.Question;
import com.online.exam.mapper.CategoryMapper;
import com.online.exam.mapper.QuestionMapper;
import com.online.exam.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(null);
    }

    @Override
    public void addCategory(String name) {
        String normalizedName = normalizeName(name);
        Long existingCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, normalizedName));
        if (existingCount > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(normalizedName);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException("分类不存在");
        }

        questionMapper.update(null, new LambdaUpdateWrapper<Question>()
                .eq(Question::getCategoryId, id)
                .set(Question::getCategoryId, null));
        categoryMapper.deleteById(id);
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("分类名称不能为空");
        }
        return name.trim();
    }
}
