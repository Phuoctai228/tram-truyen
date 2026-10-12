package com.tramtruyen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Root Staff route controller that navigates to the primary Staff CMS dashboard.
 */
@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    @GetMapping
    public String dashboard() {
        return "redirect:/staff/novels";
    }
}
