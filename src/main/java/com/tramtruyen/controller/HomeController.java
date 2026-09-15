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

    @GetMapping("/")
    public String home(Model model) {
        HomePageDTO homePageData = homeService.getHomePageData();
        
        model.addAttribute("categories", homePageData.getCategories());
        model.addAttribute("recentlyUpdatedNovels", homePageData.getRecentlyUpdatedNovels());
        model.addAttribute("hotNovels", homePageData.getHotNovels());
        model.addAttribute("completedNovels", homePageData.getCompletedNovels());
        
        return "home/index";
    }
}
