package com.tramtruyen.controller;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.service.NovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles the internal Novel CMS views and form submissions. */
@Controller
@RequestMapping("/admin/novels")
@RequiredArgsConstructor
public class NovelAdminController {

    private final NovelService novelService;

    @GetMapping
    public String listNovels(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String status,
                             Model model) {
        model.addAttribute("novels", novelService.findAdminNovels(query, status));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("status", status == null ? "" : status);
        return "admin/novels/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("novelForm", new NovelForm());
        model.addAttribute("pageTitle", "Tạo truyện");
        model.addAttribute("formAction", "/admin/novels");
        return "admin/novels/form";
    }

    @PostMapping
    public String createNovel(@Valid NovelForm form, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tạo truyện");
            model.addAttribute("formAction", "/admin/novels");
            return "admin/novels/form";
        }
        novelService.createNovel(form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo truyện thành công.");
        return "redirect:/admin/novels";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            NovelForm form = toForm(novelService.getEditableNovel(id));
            model.addAttribute("novelForm", form);
            model.addAttribute("novelId", id);
            model.addAttribute("pageTitle", "Cập nhật truyện");
            model.addAttribute("formAction", "/admin/novels/" + id);
            return "admin/novels/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    @PostMapping("/{id}")
    public String updateNovel(@PathVariable Integer id, @Valid NovelForm form,
                              BindingResult bindingResult, Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("novelId", id);
            model.addAttribute("pageTitle", "Cập nhật truyện");
            model.addAttribute("formAction", "/admin/novels/" + id);
            return "admin/novels/form";
        }
        try {
            novelService.updateNovel(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật truyện thành công.");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/novels";
    }

    @PostMapping("/{id}/delete")
    public String deleteNovel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            novelService.deleteNovel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển truyện vào lưu trữ.");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/novels";
    }

    private NovelForm toForm(com.tramtruyen.entity.Novel novel) {
        NovelForm form = new NovelForm();
        form.setTitle(novel.getTitle());
        form.setAuthor(novel.getAuthor());
        form.setSummary(novel.getSummary());
        form.setStatus(novel.getStatus());
        return form;
    }
}