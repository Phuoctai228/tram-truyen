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
    public String showCategoryDetail(
            @PathVariable String slug,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        var category = categoryService.getCategoryBySlug(slug);
        model.addAttribute("category", category);
        
        // 10 thẻ thì phân 1 trang (10 novels per page)
        int pageSize = 10;
        int currentPage = Math.max(0, page);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(currentPage, pageSize);
        org.springframework.data.domain.Page<com.tramtruyen.dto.NovelSummaryDTO> novelPage = novelService.getNovelSummariesByCategoryPage(category.getId(), pageable);
        
        model.addAttribute("novelsPage", novelPage);
        model.addAttribute("novels", novelPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", novelPage.getTotalPages());
        model.addAttribute("totalItems", novelPage.getTotalElements());
        return "categories/detail";
    }
}
