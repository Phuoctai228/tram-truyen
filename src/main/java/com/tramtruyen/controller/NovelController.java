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
    private final com.tramtruyen.service.BookshelfService bookshelfService;
    private final com.tramtruyen.service.RatingService ratingService;

    @GetMapping("/search")
    public String searchNovels(@RequestParam(required = false) String q, Model model) {
        List<Novel> results = novelService.searchPublicNovels(q);
        model.addAttribute("novels", results);
        model.addAttribute("query", q);
        return "novel/search";
    }

    @GetMapping("/truyen/{slug}")
    public String novelDetailsBySlug(
            @PathVariable String slug,
            org.springframework.security.core.Authentication authentication,
            Model model) {
        Novel novel = novelService.getPublicNovelBySlug(slug);
        List<Chapter> chapters = chapterService.getPublicChapters(novel.getId());
        boolean inBookshelf = false;
        com.tramtruyen.entity.NovelRating userRating = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
            userRating = ratingService.getUserRating(authentication.getName(), novel.getId());
        }

        long favoriteCount = bookshelfService.countNovelFavorites(novel.getId());
        List<Novel> sameAuthorNovels = novelService.getNovelsBySameAuthor(novel.getAuthor(), novel.getId());

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("sameAuthorNovels", sameAuthorNovels);
        model.addAttribute("userRating", userRating);
        return "novel/details";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/rating")
    public String submitRating(
            @PathVariable String slug,
            @RequestParam Integer rating,
            @RequestParam(required = false) String reviewText,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        ratingService.submitRating(authentication.getName(), novel.getId(), rating, reviewText);
        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho bộ truyện!");
        return "redirect:/truyen/" + novel.getSlug();
    }

    @GetMapping("/novel/{id}")
    public String novelDetailsLegacy(@PathVariable Integer id) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug();
    }

    @GetMapping({"/truyen/{slug}/chapters", "/truyen/{slug}/danh-sach-chuong"})
    public String novelChaptersBySlug(@PathVariable String slug) {
        return "redirect:/truyen/" + slug + "#chapters";
    }

    @GetMapping("/novel/{id}/chapters")
    public String novelChaptersLegacy(@PathVariable Integer id) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug() + "#chapters";
    }

    @GetMapping({"/truyen/{slug}/chuong-{chapterNumber}", "/truyen/{slug}/read/{chapterNumber}"})
    public String readChapterBySlug(
            @PathVariable String slug,
            @PathVariable Integer chapterNumber,
            org.springframework.security.core.Authentication authentication,
            Model model) {
        Novel novel = novelService.getPublicNovelBySlug(slug);
        Chapter chapter = chapterService.getPublicChapter(novel.getId(), chapterNumber);

        boolean isLoggedIn = (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken));

        if (chapter.getPrice() != null && chapter.getPrice() > 0 && !isLoggedIn) {
            return "redirect:/login?redirect=/truyen/" + novel.getSlug() + "/chuong-" + chapterNumber;
        }

        boolean inBookshelf = false;
        if (isLoggedIn) {
            bookshelfService.recordChapterRead(authentication.getName(), chapter.getId());
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
        }

        List<Chapter> chapters = chapterService.getPublicChapters(novel.getId());
        Integer prevChapterNumber = null;
        Integer nextChapterNumber = null;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getChapterNumber().equals(chapterNumber)) {
                if (i > 0) {
                    prevChapterNumber = chapters.get(i - 1).getChapterNumber();
                }
                if (i < chapters.size() - 1) {
                    nextChapterNumber = chapters.get(i + 1).getChapterNumber();
                }
                break;
            }
        }
        boolean isLatestChapter = (nextChapterNumber == null);

        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        model.addAttribute("chapters", chapters);
        model.addAttribute("prevChapterNumber", prevChapterNumber);
        model.addAttribute("nextChapterNumber", nextChapterNumber);
        model.addAttribute("isLatestChapter", isLatestChapter);
        model.addAttribute("inBookshelf", inBookshelf);
        return "novel/reading";
    }

    @GetMapping("/novel/{id}/read/{chapterNumber}")
    public String readChapterLegacy(
            @PathVariable Integer id,
            @PathVariable Integer chapterNumber) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug() + "/chuong-" + chapterNumber;
    }
}
