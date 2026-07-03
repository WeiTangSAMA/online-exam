package com.online.exam.service;

import com.online.exam.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listAll();

    void addCategory(String name);

    void deleteCategory(Long id);
}
