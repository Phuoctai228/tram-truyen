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

    @GetMapping("/")
    public String home(org.springframework.security.core.Authentication authentication, Model model) {
        HomePageDTO homePageData = homeService.getHomePageData();
        
        model.addAttribute("categories", homePageData.getCategories());
        model.addAttribute("recentlyUpdatedNovels", homePageData.getRecentlyUpdatedNovels());
        model.addAttribute("hotNovels", homePageData.getHotNovels());
        model.addAttribute("completedNovels", homePageData.getCompletedNovels());
        
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
}
