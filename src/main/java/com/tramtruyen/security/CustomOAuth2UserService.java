package com.tramtruyen.security;

import com.tramtruyen.entity.Role;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.RoleRepository;
import com.tramtruyen.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountLockService accountLockService;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   AccountLockService accountLockService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountLockService = accountLockService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Kiểm tra và tự động mở khóa nếu đã hết hạn
            if ("BANNED".equalsIgnoreCase(user.getStatus())) {
                accountLockService.checkAndAutoUnban(user);
            }

            // Nếu vẫn đang bị khóa, chặn ngay lập tức, không cập nhật DB
            if ("BANNED".equalsIgnoreCase(user.getStatus())) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("account_locked", email, null));
            }

            // Update avatar if needed
            if (user.getAvatarUrl() == null && picture != null) {
                user.setAvatarUrl(picture);
            }
            // Sync auth provider
            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
            }
            // Auto-activate if user was pending verification locally
            if ("PENDING_VERIFICATION".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
            }
            user = userRepository.save(user);
        } else {
            // Create new user
            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .avatarUrl(picture)
                    .authProvider("GOOGLE")
                    .status("ACTIVE")
                    .password(null)
                    .walletBalance(0)
                    .build();

            Role memberRole = roleRepository.findByName("ROLE_MEMBER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_MEMBER");
                        return roleRepository.save(role);
                    });
            user.getRoles().add(memberRole);

            user = userRepository.save(user);
        }

        return new CustomUserDetails(user, attributes);
    }
}
