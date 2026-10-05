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
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

            // If a user is banned, block them immediately; do not update or save anything
            // to the database
            if ("BANNED".equalsIgnoreCase(user.getStatus())) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("account_locked", "Tài khoản của bạn đã bị khóa.", null));
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
