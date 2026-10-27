package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterAccessDTO;
import com.tramtruyen.dto.ChapterAccessStatus;
import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.ChapterAccessService;
import com.tramtruyen.service.CoinPackageService;
import com.tramtruyen.util.ChapterAccessPolicy;
import com.tramtruyen.util.ChapterPreviewBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production implementation of ChapterAccessService coordinating access policy and preview creation.
 */
@Service
@RequiredArgsConstructor
public class ChapterAccessServiceImpl implements ChapterAccessService {

    private static final int DEFAULT_PACKAGE_SUGGESTION_LIMIT = 2;
    private static final DateTimeFormatter WATERMARK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;
    private final ChapterAccessPolicy chapterAccessPolicy;
    private final ChapterPreviewBuilder chapterPreviewBuilder;
    private final CoinPackageService coinPackageService;

    @Override
    @Transactional(readOnly = true)
    public ChapterAccessDTO checkAccess(Novel novel, Chapter chapter, Authentication authentication) {
        boolean loggedIn = isAuthenticated(authentication);
        boolean privileged = false;
        int walletBalance = 0;
        boolean alreadyUnlocked = false;

        String watermarkText;
        String userFingerprint;
        String nowStr = LocalDateTime.now().format(WATERMARK_DATE_FORMAT);

        if (loggedIn) {
            String email = authentication.getName();
            Collection<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            privileged = chapterAccessPolicy.isPrivileged(authorities);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                walletBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
                alreadyUnlocked = userChapterActivityRepository.findByUserAndChapter(user, chapter)
                        .map(act -> Boolean.TRUE.equals(act.getIsUnlocked()))
                        .orElse(false);
                String displayName = user.getFullName() != null && !user.getFullName().isBlank()
                        ? user.getFullName()
                        : email;
                watermarkText = "UID: " + user.getId() + " • " + displayName + " • " + nowStr + " • Trạm Truyện";
                userFingerprint = "UID:" + user.getId();
            } else {
                watermarkText = email + " • " + nowStr + " • Trạm Truyện";
                userFingerprint = email;
            }
        } else {
            watermarkText = "Khách đọc • " + nowStr + " • Trạm Truyện";
            userFingerprint = "GUEST";
        }

        ChapterAccessStatus status = chapterAccessPolicy.resolve(
                chapter, loggedIn, privileged, alreadyUnlocked, walletBalance);
        boolean isPaid = chapterAccessPolicy.isPaid(chapter);
        int price = chapter.getPrice() != null ? chapter.getPrice() : 0;
        int remainingBalance = walletBalance - price;
        int shortfall = Math.max(0, price - walletBalance);
        boolean canAfford = walletBalance >= price;
        boolean readable = status.isReadable();

        List<String> paragraphs;
        if (readable) {
            paragraphs = chapterPreviewBuilder.splitParagraphs(chapter.getContent());
        } else {
            paragraphs = chapterPreviewBuilder.buildPreview(chapter.getContent());
        }

        List<CoinPackageDTO> suggestedPackages = List.of();
        if (status == ChapterAccessStatus.INSUFFICIENT_BALANCE && shortfall > 0) {
            suggestedPackages = coinPackageService.suggestForShortfall(shortfall, DEFAULT_PACKAGE_SUGGESTION_LIMIT);
        }

        return ChapterAccessDTO.builder()
                .status(status)
                .readable(readable)
                .paid(isPaid)
                .chapterPrice(price)
                .walletBalance(walletBalance)
                .remainingBalanceAfter(remainingBalance)
                .shortfall(shortfall)
                .canAfford(canAfford)
                .loggedIn(loggedIn)
                .paragraphs(paragraphs)
                .suggestedPackages(suggestedPackages)
                .watermarkText(watermarkText)
                .userFingerprint(userFingerprint)
                .build();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
