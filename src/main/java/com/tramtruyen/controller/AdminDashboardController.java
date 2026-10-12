package com.tramtruyen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Root Admin route controller that navigates to the primary Admin CMS dashboard.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping
    public String dashboard() {
        return "redirect:/admin/novels";
    }
}
