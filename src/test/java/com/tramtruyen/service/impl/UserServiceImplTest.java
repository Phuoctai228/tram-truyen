package com.tramtruyen.service.impl;

import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.MediaStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User localUser;
    private User googleUser;

    @BeforeEach
    void setUp() {
        localUser = User.builder()
                .id(1)
                .email("local@example.com")
                .fullName("Local User")
                .password("encoded_old_pass")
                .authProvider("LOCAL")
                .status("ACTIVE")
                .build();

        googleUser = User.builder()
                .id(2)
                .email("google@example.com")
                .fullName("Google User")
                .password(null)
                .authProvider("GOOGLE")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("canChangePassword should return true for local account")
    void canChangePassword_shouldReturnTrue_forLocalAccount() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));

        boolean result = userService.canChangePassword("local@example.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canChangePassword should return false for Google account")
    void canChangePassword_shouldReturnFalse_forGoogleAccount() {
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleUser));

        boolean result = userService.canChangePassword("google@example.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canChangePassword should return false when user not found")
    void canChangePassword_shouldReturnFalse_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.canChangePassword("notfound@example.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("changePassword should throw IllegalArgumentException when account is Google")
    void changePassword_shouldThrowException_whenAccountIsGoogle() {
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> userService.changePassword("google@example.com", "anyPass", "newPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tài khoản đăng nhập bằng Google không thể đổi mật khẩu.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword should throw IllegalArgumentException when old password does not match")
    void changePassword_shouldThrowException_whenOldPasswordDoesNotMatch() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("wrongPass", "encoded_old_pass")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("local@example.com", "wrongPass", "newPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mật khẩu cũ không chính xác");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword should succeed when local account provides correct old password")
    void changePassword_shouldSucceed_whenLocalAccountAndOldPasswordMatches() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("correctOldPass", "encoded_old_pass")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded_new_pass");

        userService.changePassword("local@example.com", "correctOldPass", "newPass123");

        assertThat(localUser.getPassword()).isEqualTo("encoded_new_pass");
        verify(userRepository).save(localUser);
    }

    @Test
    @DisplayName("updateProfile should succeed when full name is valid with Vietnamese characters")
    void updateProfile_shouldSucceed_whenFullNameIsValidWithVietnameseCharacters() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));

        userService.updateProfile("local@example.com", "Nguyễn Văn Đỗ", null);

        assertThat(localUser.getFullName()).isEqualTo("Nguyễn Văn Đỗ");
        verify(userRepository).save(localUser);
    }

    @Test
    @DisplayName("updateProfile should throw IllegalArgumentException when full name contains numbers")
    void updateProfile_shouldThrowException_whenFullNameContainsNumbers() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> userService.updateProfile("local@example.com", "Nguyễn Văn A 123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Họ và tên không được chứa số và ký tự đặc biệt");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile should throw IllegalArgumentException when full name contains special characters")
    void updateProfile_shouldThrowException_whenFullNameContainsSpecialCharacters() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> userService.updateProfile("local@example.com", "Nguyễn @ Văn A", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Họ và tên không được chứa số và ký tự đặc biệt");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile should throw IllegalArgumentException when full name is blank")
    void updateProfile_shouldThrowException_whenFullNameIsBlank() {
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> userService.updateProfile("local@example.com", "   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Họ và tên không được để trống");

        verify(userRepository, never()).save(any(User.class));
    }
}
