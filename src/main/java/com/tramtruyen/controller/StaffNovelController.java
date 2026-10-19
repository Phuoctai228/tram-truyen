package com.tramtruyen.controller;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.service.CategoryService;
import com.tramtruyen.service.NovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

/**
 * Handles Staff Novel management views and operations.
 */
@Controller
@RequestMapping("/staff/novels")
@RequiredArgsConstructor
public class StaffNovelController {

    private final NovelService novelService;
    private final CategoryService categoryService;

    @GetMapping
    public String listNovels(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String status,
                             Model model) {
        model.addAttribute("novels", novelService.findAdminNovels(query, status));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("status", status == null ? "" : status);
        return "staff/novels/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("novelForm", new NovelForm());
        model.addAttribute("pageTitle", "Tạo truyện");
        model.addAttribute("formAction", "/staff/novels");
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "staff/novels/form";
    }

    @PostMapping
    public String createNovel(@Valid NovelForm form, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tạo truyện");
            model.addAttribute("formAction", "/staff/novels");
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "staff/novels/form";
        }
        novelService.createNovel(form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo truyện thành công.");
        return "redirect:/staff/novels";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            NovelForm form = toForm(novelService.getEditableNovel(id));
            model.addAttribute("novelForm", form);
            model.addAttribute("novelId", id);
            model.addAttribute("pageTitle", "Cập nhật truyện");
            model.addAttribute("formAction", "/staff/novels/" + id);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "staff/novels/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    @PostMapping("/{id}")
    public String updateNovel(@PathVariable Integer id, @Valid NovelForm form,
                              BindingResult bindingResult, Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("novelId", id);
            model.addAttribute("pageTitle", "Cập nhật truyện");
            model.addAttribute("formAction", "/staff/novels/" + id);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            form.setCurrentCoverUrl(novelService.getEditableNovel(id).getCoverUrl());
            return "staff/novels/form";
        }
        try {
            novelService.updateNovel(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật truyện thành công.");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/staff/novels";
    }

    @PostMapping("/{id}/delete")
    public String deleteNovel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            novelService.deleteNovel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển truyện vào lưu trữ.");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/staff/novels";
    }

    private NovelForm toForm(Novel novel) {
        NovelForm form = new NovelForm();
        form.setTitle(novel.getTitle());
        form.setAuthor(novel.getAuthor());
        form.setSummary(novel.getSummary());
        form.setStatus(novel.getStatus());
        form.setCurrentCoverUrl(novel.getCoverUrl());
        if (novel.getCategories() != null) {
            form.setCategoryIds(novel.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
        }
        return form;
    }
}
