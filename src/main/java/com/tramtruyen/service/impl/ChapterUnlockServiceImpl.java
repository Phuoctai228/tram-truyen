package com.tramtruyen.service.impl;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.ChapterUnlockService;
import com.tramtruyen.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Production implementation of ChapterUnlockService coordinating chapter verification and coin deduction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterUnlockServiceImpl implements ChapterUnlockService {

    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public boolean isChapterUnlocked(String email, Integer chapterId) {
        if (email == null || chapterId == null) {
            return false;
        }

        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) {
            return false;
        }

        // Free chapters are always unlocked
        if (chapter.getPrice() == null || chapter.getPrice() <= 0) {
            return true;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        return userChapterActivityRepository.findByUserAndChapter(user, chapter)
                .map(act -> Boolean.TRUE.equals(act.getIsUnlocked()))
                .orElse(false);
    }

    @Override
    @Transactional
    public UnlockResultDTO unlockChapter(String email, Integer chapterId, Boolean autoUnlockNext) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vui lòng đăng nhập để mở khóa chương"));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương truyện"));

        if (Boolean.TRUE.equals(chapter.getIsDeleted())) {
            throw new ResourceNotFoundException("Chương truyện đã bị xóa hoặc không khả dụng");
        }

        int price = chapter.getPrice() != null ? chapter.getPrice() : 0;

        // Check if already unlocked
        Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapter(user, chapter);
        if (activityOpt.isPresent() && Boolean.TRUE.equals(activityOpt.get().getIsUnlocked())) {
            int currentBalance = walletService.getBalance(email);
            return UnlockResultDTO.builder()
                    .success(true)
                    .chapterId(chapter.getId())
                    .chapterNumber(chapter.getChapterNumber())
                    .chapterTitle(chapter.getTitle())
                    .pricePaid(0)
                    .remainingBalance(currentBalance)
                    .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                    .message("Bạn đã mở khóa chương này trước đó.")
                    .build();
        }

        // Free chapter
        if (price <= 0) {
            markChapterUnlocked(user, chapter, 0, activityOpt);
            int currentBalance = walletService.getBalance(email);
            return UnlockResultDTO.builder()
                    .success(true)
                    .chapterId(chapter.getId())
                    .chapterNumber(chapter.getChapterNumber())
                    .chapterTitle(chapter.getTitle())
                    .pricePaid(0)
                    .remainingBalance(currentBalance)
                    .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                    .message("Chương miễn phí, mở khóa thành công!")
                    .build();
        }

        // Deduct coins via walletService (which enforces pessimistic write lock on user)
        String description = "Mở khóa Chương " + chapter.getChapterNumber() + ": " + chapter.getTitle();
        walletService.deductCoins(user, price, "UNLOCK_CHAPTER", chapter.getId(), description);

        // Record unlock in user_chapter_activities
        markChapterUnlocked(user, chapter, price, activityOpt);

        int remainingBalance = walletService.getBalance(email);

        log.info("User {} successfully unlocked chapter {} ({} Coin)", email, chapterId, price);

        return UnlockResultDTO.builder()
                .success(true)
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .pricePaid(price)
                .remainingBalance(remainingBalance)
                .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                .message("Mở khóa chương thành công!")
                .build();
    }

    private void markChapterUnlocked(User user, Chapter chapter, int price, Optional<UserChapterActivity> existingOpt) {
        UserChapterActivity activity = existingOpt.orElseGet(() -> UserChapterActivity.builder()
                .user(user)
                .chapter(chapter)
                .build());

        activity.setIsUnlocked(true);
        activity.setPricePaid(price);
        activity.setUnlockedAt(LocalDateTime.now());
        userChapterActivityRepository.save(activity);
    }
}
