package com.tramtruyen.security;

import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountLockService accountLockService;

    public CustomUserDetailsService(UserRepository userRepository, AccountLockService accountLockService) {
        this.userRepository = userRepository;
        this.accountLockService = accountLockService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + username));

        // Kiểm tra và tự động mở khóa nếu thời hạn khóa đã hết
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            accountLockService.checkAndAutoUnban(user);
        }

        return new CustomUserDetails(user);
    }
}
