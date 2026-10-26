package com.tramtruyen.controller;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.dto.ChapterSaveAction;
import com.tramtruyen.entity.Chapter;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles Staff Chapter management views and operations.
 */
@Controller
@RequestMapping("/staff/novels/{novelId}/chapters")
@RequiredArgsConstructor
public class StaffChapterController {

    private final ChapterService chapterService;
    private final NovelService novelService;

    @GetMapping
    public String listChapters(@PathVariable Integer novelId, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Novel novel = novelService.getEditableNovel(novelId);
            model.addAttribute("novel", novel);
            model.addAttribute("chapters", chapterService.findNovelChapters(novelId));
            return "staff/chapters/list";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Integer novelId, Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", new ChapterForm());
            model.addAttribute("pageTitle", "Tạo chương mới");
            model.addAttribute("formAction", "/staff/novels/" + novelId + "/chapters");
            return "staff/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    @PostMapping
    public String createChapter(@PathVariable Integer novelId, @Valid ChapterForm form,
                                BindingResult bindingResult,
                                @RequestParam(defaultValue = "DRAFT") ChapterSaveAction saveAction,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Tạo chương mới", "/staff/novels/" + novelId + "/chapters");
        }

        try {
            Chapter chapter = chapterService.createChapter(novelId, form, saveAction);
            redirectAttributes.addFlashAttribute("successMessage",
                    saveAction == ChapterSaveAction.PUBLISHED
                            ? "Đã tạo và xuất bản chương thành công."
                            : "Đã lưu chương ở dạng bản nháp.");
            if (saveAction == ChapterSaveAction.DRAFT) {
                return "redirect:/staff/novels/" + novelId + "/chapters/" + chapter.getId() + "/edit";
            }
            return "redirect:/staff/novels/" + novelId + "/chapters";
        } catch (DuplicateResourceException exception) {
            bindingResult.rejectValue("chapterNumber", "duplicate", exception.getMessage());
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Tạo chương mới", "/staff/novels/" + novelId + "/chapters");
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    @GetMapping("/{chapterId}/edit")
    public String editForm(@PathVariable Integer novelId, @PathVariable Integer chapterId,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", toForm(chapterService.getEditableChapter(novelId, chapterId)));
            model.addAttribute("pageTitle", "Chỉnh sửa chương");
            model.addAttribute("formAction", "/staff/novels/" + novelId + "/chapters/" + chapterId);
            return "staff/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    @PostMapping("/{chapterId}")
    public String updateChapter(@PathVariable Integer novelId, @PathVariable Integer chapterId,
                                @Valid ChapterForm form, BindingResult bindingResult,
                                @RequestParam(defaultValue = "DRAFT") ChapterSaveAction saveAction,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Chỉnh sửa chương", "/staff/novels/" + novelId + "/chapters/" + chapterId);
        }
        try {
            chapterService.updateChapter(novelId, chapterId, form, saveAction);
            redirectAttributes.addFlashAttribute("successMessage",
                    saveAction == ChapterSaveAction.PUBLISHED
                            ? "Đã cập nhật và xuất bản chương thành công."
                            : "Đã lưu thay đổi ở dạng bản nháp.");
        } catch (DuplicateResourceException exception) {
            bindingResult.rejectValue("chapterNumber", "duplicate", exception.getMessage());
            return renderFormWithErrors(novelId, form, model, redirectAttributes,
                    "Chỉnh sửa chương", "/staff/novels/" + novelId + "/chapters/" + chapterId);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels/" + novelId + "/chapters";
        }
        if (saveAction == ChapterSaveAction.DRAFT) {
            return "redirect:/staff/novels/" + novelId + "/chapters/" + chapterId + "/edit";
        }
        return "redirect:/staff/novels/" + novelId + "/chapters";
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
        return "redirect:/staff/novels/" + novelId + "/chapters";
    }

    private String renderFormWithErrors(Integer novelId, ChapterForm form, Model model,
                                        RedirectAttributes redirectAttributes, String pageTitle,
                                        String formAction) {
        try {
            model.addAttribute("novel", novelService.getEditableNovel(novelId));
            model.addAttribute("chapterForm", form);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("formAction", formAction);
            return "staff/chapters/form";
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/staff/novels";
        }
    }

    private ChapterForm toForm(Chapter chapter) {
        ChapterForm form = new ChapterForm();
        form.setChapterNumber(chapter.getChapterNumber());
        form.setTitle(chapter.getTitle());
        form.setContent(chapter.getContent());
        form.setPrice(chapter.getPrice());
        return form;
    }
}
