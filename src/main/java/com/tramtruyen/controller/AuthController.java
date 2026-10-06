package com.tramtruyen.controller;

import com.tramtruyen.dto.AccountLockedInfoDTO;
import com.tramtruyen.dto.RegisterRequestDTO;
import com.tramtruyen.service.AuthService;
import com.tramtruyen.security.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            Model model) {
        String targetRedirect = redirect;
        if (targetRedirect == null || targetRedirect.isBlank() || "/".equals(targetRedirect)) {
            Object sessionRedirect = request.getSession().getAttribute("REDIRECT_URL");
            if (sessionRedirect instanceof String s && !s.isBlank() && !"/".equals(s)) {
                targetRedirect = s;
            } else {
                targetRedirect = null;
            }
        }

        if (targetRedirect != null && !targetRedirect.isBlank() && CustomAuthenticationSuccessHandler.isValidRedirectUrl(targetRedirect)) {
            request.getSession().setAttribute("REDIRECT_URL", targetRedirect);
            model.addAttribute("redirect", targetRedirect);
        } else {
            request.getSession().removeAttribute("REDIRECT_URL");
            model.addAttribute("redirect", null);
        }
        return "authentication/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            Model model) {
        String targetRedirect = redirect;
        if (targetRedirect == null || targetRedirect.isBlank() || "/".equals(targetRedirect)) {
            Object sessionRedirect = request.getSession().getAttribute("REDIRECT_URL");
            if (sessionRedirect instanceof String s && !s.isBlank() && !"/".equals(s)) {
                targetRedirect = s;
            } else {
                targetRedirect = null;
            }
        }

        if (targetRedirect != null && !targetRedirect.isBlank() && CustomAuthenticationSuccessHandler.isValidRedirectUrl(targetRedirect)) {
            request.getSession().setAttribute("REDIRECT_URL", targetRedirect);
            model.addAttribute("redirect", targetRedirect);
        } else {
            request.getSession().removeAttribute("REDIRECT_URL");
            model.addAttribute("redirect", null);
        }
        model.addAttribute("userDto", new RegisterRequestDTO());
        return "authentication/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("userDto") RegisterRequestDTO userDto,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "authentication/register";
        }

        try {
            authService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng kiểm tra email để nhận mã OTP.");
            return "redirect:/verify-otp?email=" + userDto.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "authentication/register";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            return "authentication/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(@org.springframework.web.bind.annotation.RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "authentication/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@org.springframework.web.bind.annotation.RequestParam("email") String email,
                                   @org.springframework.web.bind.annotation.RequestParam("otpCode") String otpCode,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            authService.verifyOtp(email, otpCode);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "authentication/verify-otp";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            model.addAttribute("email", email);
            return "authentication/verify-otp";
        }
    }

    @GetMapping("/account-locked")
    public String showAccountLockedPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        Object lockedInfoObj = session != null ? session.getAttribute("LOCKED_USER_INFO") : null;
        if (lockedInfoObj instanceof AccountLockedInfoDTO lockedInfo) {
            session.removeAttribute("LOCKED_USER_INFO");
            model.addAttribute("lockedInfo", lockedInfo);
            return "authentication/account-locked";
        }
        return "redirect:/login";
    }
}
