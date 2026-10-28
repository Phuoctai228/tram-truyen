package com.tramtruyen.controller;

import com.tramtruyen.dto.HomePageDTO;
import com.tramtruyen.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final com.tramtruyen.service.BookshelfService bookshelfService;
    private final com.tramtruyen.service.SystemSettingService systemSettingService;

    @GetMapping("/")
    public String home(org.springframework.security.core.Authentication authentication, Model model) {
        HomePageDTO homePageData = homeService.getHomePageData();
        
        model.addAttribute("categories", homePageData.getCategories());
        model.addAttribute("recentlyUpdatedNovels", homePageData.getRecentlyUpdatedNovels());
        model.addAttribute("hotNovels", homePageData.getHotNovels());
        model.addAttribute("completedNovels", homePageData.getCompletedNovels());
        model.addAttribute("recommendedNovels", homePageData.getRecommendedNovels());
        model.addAttribute("newNovels", homePageData.getNewNovels());
        
        java.util.List<com.tramtruyen.dto.NovelSummaryDTO> hotList = homePageData.getHotNovels();
        java.util.List<com.tramtruyen.dto.NovelSummaryDTO> heroNovels = (hotList != null && hotList.size() > 5)
                ? hotList.subList(0, 5)
                : (hotList != null ? hotList : java.util.Collections.emptyList());
        model.addAttribute("heroNovels", heroNovels);
        
        boolean heroInBookshelf = false;
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            if (homePageData.getHotNovels() != null && !homePageData.getHotNovels().isEmpty()) {
                Integer heroNovelId = homePageData.getHotNovels().get(0).getId();
                heroInBookshelf = bookshelfService.isNovelInBookshelf(authentication.getName(), heroNovelId);
            }
        }
        model.addAttribute("heroInBookshelf", heroInBookshelf);
        
        return "home/index";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("title", "Điều khoản dịch vụ");
        model.addAttribute("type", "TERMS");
        model.addAttribute("settings", systemSettingService.getSettings());
        return "home/policy";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("title", "Chính sách bảo mật");
        model.addAttribute("type", "PRIVACY");
        model.addAttribute("settings", systemSettingService.getSettings());
        return "home/policy";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Liên hệ & Hỗ trợ");
        model.addAttribute("type", "CONTACT");
        model.addAttribute("settings", systemSettingService.getSettings());
        return "home/policy";
    }
}
