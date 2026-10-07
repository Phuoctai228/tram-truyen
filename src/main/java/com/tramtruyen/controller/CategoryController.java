package com.tramtruyen.controller;

import com.tramtruyen.dto.CategoryRequestDTO;
import com.tramtruyen.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        if (!model.containsAttribute("categoryRequestDTO")) {
            model.addAttribute("categoryRequestDTO", new CategoryRequestDTO());
        }
        return "admin/categories/list";
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("categoryRequestDTO") CategoryRequestDTO categoryRequestDTO,
                                 BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryRequestDTO", result);
            redirectAttributes.addFlashAttribute("categoryRequestDTO", categoryRequestDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập");
            redirectAttributes.addFlashAttribute("openAddModal", true);
            return "redirect:/admin/categories";
        }
        try {
            categoryService.createCategory(categoryRequestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thể loại mới thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("categoryRequestDTO", categoryRequestDTO);
            redirectAttributes.addFlashAttribute("openAddModal", true);
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable Integer id,
                                 @Valid @ModelAttribute("categoryRequestDTO") CategoryRequestDTO categoryRequestDTO,
                                 BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryRequestDTO", result);
            redirectAttributes.addFlashAttribute("categoryRequestDTO", categoryRequestDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập");
            redirectAttributes.addFlashAttribute("editModalId", id);
            return "redirect:/admin/categories";
        }
        try {
            categoryService.updateCategory(id, categoryRequestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thể loại thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("categoryRequestDTO", categoryRequestDTO);
            redirectAttributes.addFlashAttribute("editModalId", id);
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thể loại thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thể loại. Có thể do ràng buộc dữ liệu.");
        }
        return "redirect:/admin/categories";
    }
}
