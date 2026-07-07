package com.online.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.online.exam.entity.Category;
import com.online.exam.entity.Question;
import com.online.exam.mapper.CategoryMapper;
import com.online.exam.mapper.QuestionMapper;
import com.online.exam.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Category category = new Category();
        category.setName(name);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        questionMapper.update(null, new LambdaUpdateWrapper<Question>()
                .eq(Question::getCategoryId, id)
                .set(Question::getCategoryId, null));
        categoryMapper.deleteById(id);
    }
}
