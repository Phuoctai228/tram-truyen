package com.tramtruyen.repository;

import com.tramtruyen.entity.EmailVerificationToken;
import com.tramtruyen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {
    Optional<EmailVerificationToken> findByOtpCodeAndUser(String otpCode, User user);
    void deleteByUser(User user);
}
