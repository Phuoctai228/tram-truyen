package com.tramtruyen.controller;

import com.tramtruyen.dto.HomePageDTO;
import com.tramtruyen.dto.LeaderboardItemDTO;
import com.tramtruyen.dto.NovelSummaryDTO;
import com.tramtruyen.service.BookshelfService;
import com.tramtruyen.service.HomeService;
import com.tramtruyen.service.LeaderboardService;
import com.tramtruyen.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final BookshelfService bookshelfService;
    private final SystemSettingService systemSettingService;
    private final LeaderboardService leaderboardService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        HomePageDTO homePageData = homeService.getHomePageData();
        bindHomePageDataToModel(model, homePageData);

        List<NovelSummaryDTO> heroNovels = resolveHeroNovels(homePageData.getHotNovels(), 5);
        model.addAttribute("heroNovels", heroNovels);
        model.addAttribute("heroInBookshelf", isHeroInBookshelf(authentication, heroNovels));

        return "home/index";
    }

    private void bindHomePageDataToModel(Model model, HomePageDTO homePageData) {
        model.addAttribute("categories", homePageData.getCategories());
        model.addAttribute("recentlyUpdatedNovels", homePageData.getRecentlyUpdatedNovels());
        model.addAttribute("hotNovels", homePageData.getHotNovels());
        model.addAttribute("completedNovels", homePageData.getCompletedNovels());
        model.addAttribute("recommendedNovels", homePageData.getRecommendedNovels());
        model.addAttribute("newNovels", homePageData.getNewNovels());
    }

    private List<NovelSummaryDTO> resolveHeroNovels(List<NovelSummaryDTO> hotList, int limit) {
        if (hotList == null || hotList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, NovelSummaryDTO> summaryMap = hotList.stream()
                .collect(Collectors.toMap(NovelSummaryDTO::getId, dto -> dto, (existing, replacing) -> existing));

        List<LeaderboardItemDTO> weekTopItems = leaderboardService.getLeaderboardItems("week", limit);
        List<NovelSummaryDTO> heroNovels = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();

        if (weekTopItems != null) {
            for (LeaderboardItemDTO item : weekTopItems) {
                if (item.getNovelId() != null && summaryMap.containsKey(item.getNovelId())) {
                    heroNovels.add(summaryMap.get(item.getNovelId()));
                    addedIds.add(item.getNovelId());
                }
            }
        }

        // Fallback: nếu chưa đủ limit từ leaderboard tuần, bổ sung từ hotList gốc
        for (NovelSummaryDTO dto : hotList) {
            if (heroNovels.size() >= limit) {
                break;
            }
            if (addedIds.add(dto.getId())) {
                heroNovels.add(dto);
            }
        }

        return heroNovels;
    }

    private boolean isHeroInBookshelf(Authentication authentication, List<NovelSummaryDTO> heroNovels) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (heroNovels != null && !heroNovels.isEmpty()) {
                Integer heroNovelId = heroNovels.get(0).getId();
                return bookshelfService.isNovelInBookshelf(authentication.getName(), heroNovelId);
            }
        }
        return false;
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
