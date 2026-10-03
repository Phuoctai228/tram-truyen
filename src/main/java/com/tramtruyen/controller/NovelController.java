package com.tramtruyen.controller;

import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.service.ChapterService;
import com.tramtruyen.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final ChapterService chapterService;

    @GetMapping("/search")
    public String searchNovels(@RequestParam(required = false) String q, Model model) {
        List<Novel> results = novelService.searchPublicNovels(q);
        model.addAttribute("novels", results);
        model.addAttribute("query", q);
        return "novel/search";
    }

    @GetMapping("/novel/{id}")
    public String novelDetails(@PathVariable Integer id, Model model) {
        Novel novel = novelService.getPublicNovel(id);
        List<Chapter> chapters = chapterService.getPublicChapters(id);
        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        return "novel/details";
    }

    @GetMapping("/novel/{id}/chapters")
    public String novelChapters(@PathVariable Integer id) {
        return "redirect:/novel/" + id + "#chapters";
    }

    @GetMapping("/novel/{id}/read/{chapterNumber}")
    public String readChapter(@PathVariable Integer id, @PathVariable Integer chapterNumber, Model model) {
        Novel novel = novelService.getPublicNovel(id);
        Chapter chapter = chapterService.getPublicChapter(id, chapterNumber);
        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        return "novel/reading";
    }
}
