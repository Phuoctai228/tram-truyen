package com.tramtruyen.controller;

import com.tramtruyen.dto.AssignRoleRequest;
import com.tramtruyen.dto.BanUserRequest;
import com.tramtruyen.dto.UnbanUserRequest;
import com.tramtruyen.dto.UserManagementDTO;
import com.tramtruyen.dto.UserStatsDTO;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {

    private final UserManagementService userManagementService;
    private final UserRepository userRepository;

    @GetMapping
    public String listUsers(@RequestParam(required = false, defaultValue = "") String keyword,
                            @RequestParam(required = false, defaultValue = "") String role,
                            @RequestParam(required = false, defaultValue = "") String status,
                            @RequestParam(required = false, defaultValue = "0") int page,
                            @RequestParam(required = false, defaultValue = "20") int size,
                            Authentication authentication,
                            Model model) {

        Page<UserManagementDTO> usersPage = userManagementService.getUsers(keyword, role, status, page, size);
        UserStatsDTO stats = userManagementService.getUserStats();

        Integer currentAdminId = getAdminId(authentication);

        model.addAttribute("users", usersPage);
        model.addAttribute("stats", stats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentAdminId", currentAdminId);
        model.addAttribute("pageTitle", "Quản lý người dùng & phân quyền | Trạm Truyện");

        return "admin/users/list";
    }

    @PostMapping("/assign-role")
    public String assignRole(@Valid @ModelAttribute AssignRoleRequest request,
                             BindingResult bindingResult,
                             Authentication authentication,
                             HttpServletRequest httpRequest,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users";
        }

        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            userManagementService.assignRole(adminId, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền người dùng thành công.");
        } catch (IllegalArgumentException e) {
            if ("NO_CHANGE".equals(e.getMessage())) {
                redirectAttributes.addFlashAttribute("infoMessage", "Không có thay đổi.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/ban")
    public String banUser(@Valid @ModelAttribute BanUserRequest request,
                          BindingResult bindingResult,
                          Authentication authentication,
                          HttpServletRequest httpRequest,
                          RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users";
        }

        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            userManagementService.banUser(adminId, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Khóa tài khoản thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/unban")
    public String unbanUser(@Valid @ModelAttribute UnbanUserRequest request,
                            BindingResult bindingResult,
                            Authentication authentication,
                            HttpServletRequest httpRequest,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users";
        }

        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            userManagementService.unbanUser(adminId, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phục tài khoản thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    private Integer getAdminId(Authentication authentication) {
        if (authentication == null) {
            return 1;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .map(com.tramtruyen.entity.User::getId)
                .orElse(1);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
