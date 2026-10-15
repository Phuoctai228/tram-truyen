package com.tramtruyen.controller;

import com.tramtruyen.dto.LeaderboardDTO;
import com.tramtruyen.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for presenting the novel leaderboard / ranking page.
 */
@Controller
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/bang-xep-hang")
    public String showLeaderboard(
            @RequestParam(value = "category", required = false) Integer categoryId,
            Model model) {

        LeaderboardDTO leaderboard = leaderboardService.getLeaderboard(categoryId);
        model.addAttribute("leaderboard", leaderboard);
        return "leaderboard/index";
    }
}
