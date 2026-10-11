package com.tramtruyen.controller;

import com.tramtruyen.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tramtruyen.service.NovelService;

@Controller
@RequiredArgsConstructor
public class PublicCategoryController {
    
    private final CategoryService categoryService;
    private final NovelService novelService;

    @GetMapping("/the-loai")
    public String showCategories(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("categories", categoryService.searchCategories(query));
            model.addAttribute("query", query);
        } else {
            model.addAttribute("categories", categoryService.getAllCategories());
        }
        return "categories/index";
    }

    @GetMapping("/the-loai/{slug}")
    public String showCategoryDetail(@PathVariable String slug, Model model) {
        var category = categoryService.getCategoryBySlug(slug);
        model.addAttribute("category", category);
        model.addAttribute("novels", novelService.getNovelsByCategory(category.getId()));
        return "categories/detail";
    }
}
