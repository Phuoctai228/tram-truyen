package com.tramtruyen.controller;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.CategoryService;
import com.tramtruyen.service.ChapterService;
import com.tramtruyen.service.ChapterUnlockService;
import com.tramtruyen.service.NovelService;
import com.tramtruyen.service.RatingService;
import com.tramtruyen.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final BookshelfService bookshelfService;
    private final RatingService ratingService;
    private final CategoryService categoryService;
    private final ChapterUnlockService chapterUnlockService;
    private final WalletService walletService;
    private final com.tramtruyen.service.CommentService commentService;
    private final com.tramtruyen.service.ChapterAccessService chapterAccessService;

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
            
        if (minRating != null && minRating <= 0.0) {
            minRating = null;
        }

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
        boolean hasReadAnyChapter = false;
        com.tramtruyen.entity.NovelRating userRating = null;
        Chapter lastReadChapter = null;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
            hasReadAnyChapter = bookshelfService.hasReadAnyChapter(authentication.getName(), novel.getId());
            userRating = ratingService.getUserRating(authentication.getName(), novel.getId());
            lastReadChapter = bookshelfService.getLastReadChapter(authentication.getName(), novel.getId()).orElse(null);
        }

        long favoriteCount = bookshelfService.countNovelFavorites(novel.getId());
        List<Novel> sameAuthorNovels = novelService.getNovelsBySameAuthor(novel.getAuthor(), novel.getId());
        List<com.tramtruyen.entity.NovelRating> allRatings = ratingService.getRatingsByNovel(novel.getId());
        List<com.tramtruyen.entity.Comment> allComments = commentService.getVisibleCommentsByNovel(novel.getId());

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("lastReadChapter", lastReadChapter);
        model.addAttribute("hasReadAnyChapter", hasReadAnyChapter);
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("sameAuthorNovels", sameAuthorNovels);
        model.addAttribute("userRating", userRating);
        model.addAttribute("allRatings", allRatings);
        model.addAttribute("allComments", allComments);
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
        return "redirect:/truyen/" + novel.getSlug() + "#ratings";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/rating/delete")
    public String deleteRating(
            @PathVariable String slug,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        try {
            ratingService.deleteRating(authentication.getName(), novel.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá của bạn thành công. Bây giờ bạn có thể đánh giá lại.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi xóa đánh giá.");
        }
        return "redirect:/truyen/" + novel.getSlug() + "#ratings";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/rating/edit")
    public String editReview(
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
            ratingService.updateRating(authentication.getName(), novel.getId(), rating, reviewText);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật đánh giá của bạn thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi cập nhật đánh giá.");
        }
        return "redirect:/truyen/" + novel.getSlug() + "#ratings";
    }

    // -------------------------------------------------------------------------
    // BÌNH LUẬN TRUYỆN (Facebook-style Novel Discussion)
    // -------------------------------------------------------------------------

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/comments")
    public String addNovelComment(
            @PathVariable String slug,
            @RequestParam String content,
            @RequestParam(required = false) Integer parentId,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        try {
            commentService.addNovelComment(authentication.getName(), novel.getId(), parentId, content);
            redirectAttributes.addFlashAttribute("successMessage", parentId != null ? "Đã gửi câu trả lời bình luận!" : "Đã gửi bình luận thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi gửi bình luận.");
        }
        return "redirect:/truyen/" + novel.getSlug() + "#comments";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/comments/{commentId}/edit")
    public String editNovelComment(
            @PathVariable String slug,
            @PathVariable Integer commentId,
            @RequestParam String content,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        try {
            commentService.updateComment(authentication.getName(), commentId, content);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bình luận thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật bình luận.");
        }
        return "redirect:/truyen/" + novel.getSlug() + "#comments";
    }

    @org.springframework.web.bind.annotation.PostMapping("/truyen/{slug}/comments/{commentId}/delete")
    public String deleteNovelComment(
            @PathVariable String slug,
            @PathVariable Integer commentId,
            org.springframework.security.core.Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        Novel novel = novelService.getPublicNovelBySlug(slug);
        try {
            commentService.deleteComment(authentication.getName(), commentId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bình luận thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa bình luận.");
        }
        return "redirect:/truyen/" + novel.getSlug() + "#comments";
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
            Authentication authentication,
            HttpServletRequest request,
            Model model) {
        Novel novel = novelService.getPublicNovelBySlug(slug);
        Chapter chapter = chapterService.getPublicChapter(novel.getId(), chapterNumber);

        // Deduplicate view increment per session (prevents double counting from prefetch, double-click, F5 reload)
        HttpSession session = request.getSession(true);
        @SuppressWarnings("unchecked")
        Set<Integer> viewedChapters = (Set<Integer>) session.getAttribute("VIEWED_CHAPTER_IDS");
        if (viewedChapters == null) {
            viewedChapters = new HashSet<>();
            session.setAttribute("VIEWED_CHAPTER_IDS", viewedChapters);
        }
        if (viewedChapters.add(chapter.getId())) {
            chapterService.recordView(novel.getId(), chapter.getId());
        }

        boolean isLoggedIn = (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken));

        // Reading activity tracking
        boolean isChapterRead = false;
        boolean inBookshelf = false;
        if (isLoggedIn) {
            inBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), novel.getId());
            isChapterRead = bookshelfService.isChapterRead(authentication.getName(), chapter.getId());
        }

        // Delegate content security, access resolution and top-up recommendations to service
        com.tramtruyen.dto.ChapterAccessDTO access =
                chapterAccessService.checkAccess(novel, chapter, authentication);

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

        // VIP Unlock & Access attributes for reading view and modals
        model.addAttribute("chapterAccess", access);
        model.addAttribute("accessStatus", access.getStatus().name());
        model.addAttribute("isVip", access.isPaid());
        model.addAttribute("isChapterUnlocked", access.isReadable());
        model.addAttribute("paragraphs", access.getParagraphs());
        model.addAttribute("userWalletBalance", access.getWalletBalance());
        model.addAttribute("remainingBalanceAfter", access.getRemainingBalanceAfter());
        model.addAttribute("canAfford", access.isCanAfford());
        model.addAttribute("shortfall", access.getShortfall());
        model.addAttribute("isLoggedIn", access.isLoggedIn());
        model.addAttribute("suggestedPackages", access.getSuggestedPackages());
        model.addAttribute("watermarkText", access.getWatermarkText());
        model.addAttribute("userFingerprint", access.getUserFingerprint());

        return "novel/reading";
    }

    @PostMapping("/truyen/{slug}/chuong-{chapterNumber}/unlock")
    public Object unlockChapter(
            @PathVariable String slug,
            @PathVariable Integer chapterNumber,
            @RequestParam(defaultValue = "false") Boolean autoUnlockNext,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            if (isAjax) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để mở khóa chương"));
            }
            return "redirect:/login?redirect=/truyen/" + slug + "/chuong-" + chapterNumber;
        }

        Novel novel = novelService.getPublicNovelBySlug(slug);
        Chapter chapter = chapterService.getPublicChapter(novel.getId(), chapterNumber);

        UnlockResultDTO result = chapterUnlockService.unlockChapter(
                authentication.getName(), chapter.getId(), autoUnlockNext);

        if (isAjax) {
            return ResponseEntity.ok(result);
        }

        redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        return "redirect:/truyen/" + slug + "/chuong-" + chapterNumber;
    }

    @GetMapping("/novel/{id}/read/{chapterNumber}")
    public String readChapterLegacy(
            @PathVariable Integer id,
            @PathVariable Integer chapterNumber) {
        Novel novel = novelService.getPublicNovel(id);
        return "redirect:/truyen/" + novel.getSlug() + "/chuong-" + chapterNumber;
    }
}
