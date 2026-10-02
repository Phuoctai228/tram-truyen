package com.tramtruyen.controller;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.service.ChapterService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles chapter management views for the Novel CMS. */
@Controller
@RequestMapping("/admin/novels/{novelId}/chapters")
@RequiredArgsConstructor
public class ChapterAdminController {

    private final ChapterService chapterService;
    private final NovelService novelService;

    @GetMapping
    public String listChapters(@PathVariable Integer novelId, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Novel novel = novelService.getEditableNovel(novelId);
            model.addAttribute("novel", novel);
            model.addAttribute("chapters", chapterService.findNovelChapters(novelId));
            return "admin/chapters/list";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Integer novelId, Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", new ChapterForm());
            model.addAttribute("pageTitle", "Tạo chương mới");
            model.addAttribute("formAction", "/admin/novels/" + novelId + "/chapters");
            return "admin/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    @PostMapping
    public String createChapter(@PathVariable Integer novelId, @Valid ChapterForm form,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Tạo chương mới", "/admin/novels/" + novelId + "/chapters");
        }

        try {
            chapterService.createChapter(novelId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo chương thành công.");
            return "redirect:/admin/novels/" + novelId + "/chapters";
        } catch (DuplicateResourceException exception) {
            bindingResult.rejectValue("chapterNumber", "duplicate", exception.getMessage());
                return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Tạo chương mới", "/admin/novels/" + novelId + "/chapters");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    @GetMapping("/{chapterId}/edit")
    public String editForm(@PathVariable Integer novelId, @PathVariable Integer chapterId,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", toForm(chapterService.getEditableChapter(novelId, chapterId)));
            model.addAttribute("pageTitle", "Chỉnh sửa chương");
            model.addAttribute("formAction", "/admin/novels/" + novelId + "/chapters/" + chapterId);
            return "admin/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    @PostMapping("/{chapterId}")
    public String updateChapter(@PathVariable Integer novelId, @PathVariable Integer chapterId,
                                @Valid ChapterForm form, BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Chỉnh sửa chương", "/admin/novels/" + novelId + "/chapters/" + chapterId);
        }
        try {
            chapterService.updateChapter(novelId, chapterId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật chương thành công.");
        } catch (DuplicateResourceException exception) {
            bindingResult.rejectValue("chapterNumber", "duplicate", exception.getMessage());
                return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Chỉnh sửa chương", "/admin/novels/" + novelId + "/chapters/" + chapterId);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/novels/" + novelId + "/chapters";
    }

    @PostMapping("/{chapterId}/delete")
    public String deleteChapter(@PathVariable Integer novelId, @PathVariable Integer chapterId,
                                RedirectAttributes redirectAttributes) {
        try {
            chapterService.deleteChapter(novelId, chapterId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mềm chương thành công.");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/novels/" + novelId + "/chapters";
    }

    private String renderFormWithErrors(Integer novelId, ChapterForm form, Model model,
                                        RedirectAttributes redirectAttributes, String pageTitle,
                                        String formAction) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", form);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("formAction", formAction);
            return "admin/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/novels";
        }
    }

    private ChapterForm toForm(com.tramtruyen.entity.Chapter chapter) {
        ChapterForm form = new ChapterForm();
        form.setChapterNumber(chapter.getChapterNumber());
        form.setTitle(chapter.getTitle());
        form.setContent(chapter.getContent());
        return form;
    }
}