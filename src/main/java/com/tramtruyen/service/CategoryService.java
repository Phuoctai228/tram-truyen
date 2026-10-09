package com.tramtruyen.service;

import com.tramtruyen.dto.CategoryRequestDTO;
import com.tramtruyen.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(CategoryRequestDTO request);
    Category updateCategory(Integer id, CategoryRequestDTO request);
    void deleteCategory(Integer id);
    Category getCategoryById(Integer id);
    List<Category> searchCategories(String keyword);
    Category getCategoryBySlug(String slug);
}
