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

    private final com.tramtruyen.service.CategoryService categoryService;

    @GetMapping("/search")
    public String searchNovels(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Integer> categories,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String chapterRange,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            Model model) {
            
        Integer minChapters = null;
        Integer maxChapters = null;
        if (chapterRange != null) {
            switch (chapterRange) {
                case "lt5": maxChapters = 4; break;
                case "5-10": minChapters = 5; maxChapters = 10; break;
                case "10-50": minChapters = 11; maxChapters = 50; break;
                case "gt50": minChapters = 51; break;
            }
        }

        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? 
                org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortParams[0]));

        org.springframework.data.domain.Page<Novel> results = novelService.advancedSearch(q, categories, status, minChapters, maxChapters, minRating, pageable);
        
        model.addAttribute("novelPage", results);
        model.addAttribute("novels", results.getContent());
        model.addAttribute("query", q);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        
        // Pass back current filters for UI state
        model.addAttribute("selectedCategories", categories != null ? categories : new java.util.ArrayList<>());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedChapterRange", chapterRange);
        model.addAttribute("selectedMinRating", minRating);
        model.addAttribute("currentSort", sort);

        return "home/search";
    }

    @GetMapping("/api/search/suggestions")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> searchSuggestions(@RequestParam(required = false) String q) {
        List<Novel> results = novelService.searchPublicNovels(q);
        // limit to 5
        if (results != null && results.size() > 5) {
            results = results.subList(0, 5);
        }
        
        List<java.util.Map<String, Object>> suggestions = new java.util.ArrayList<>();
        if (results != null) {
            for (Novel n : results) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("slug", n.getSlug());
                map.put("title", n.getTitle());
                map.put("author", n.getAuthor());
                map.put("coverUrl", n.getCoverUrl());
                // category logic (pick first)
                if (n.getCategories() != null && !n.getCategories().isEmpty()) {
                    map.put("category", n.getCategories().iterator().next().getName());
                } else {
                    map.put("category", "Khác");
                }
                suggestions.add(map);
            }
        }
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("results", suggestions);
        response.put("total", novelService.searchPublicNovels(q).size());
        return response;
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
        List<com.tramtruyen.entity.NovelRating> allRatings = ratingService.getRatingsByNovel(novel.getId());

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("sameAuthorNovels", sameAuthorNovels);
        model.addAttribute("userRating", userRating);
        model.addAttribute("allRatings", allRatings);
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
        try {
            ratingService.submitRating(authentication.getName(), novel.getId(), rating, reviewText);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho bộ truyện!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi gửi đánh giá. Vui lòng thử lại sau.");
        }
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

        boolean isChapterRead = false;
        boolean inBookshelf = false;
        if (isLoggedIn) {
            isChapterRead = bookshelfService.isChapterRead(authentication.getName(), chapter.getId());
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
        model.addAttribute("isChapterRead", isChapterRead);
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
