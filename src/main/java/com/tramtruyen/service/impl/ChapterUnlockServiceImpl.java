package com.tramtruyen.service.impl;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.entity.enums.NovelStatus;
import com.tramtruyen.entity.enums.TransactionType;
import com.tramtruyen.exception.ChapterNotPurchasableException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.ChapterUnlockService;
import com.tramtruyen.service.WalletService;
import com.tramtruyen.util.ChapterAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling chapter unlocking and coin deduction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterUnlockServiceImpl implements ChapterUnlockService {

    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;
    private final WalletService walletService;
    private final ChapterAccessPolicy chapterAccessPolicy;

    /**
     * Checks if a chapter has already been unlocked by the user.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isChapterUnlocked(String email, Integer chapterId) {
        if (chapterId == null) {
            return false;
        }

        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null || Boolean.TRUE.equals(chapter.getIsDeleted())) {
            return false;
        }

        // Free chapters are always accessible (even for unauthenticated visitors)
        if (!chapterAccessPolicy.isPaid(chapter)) {
            return true;
        }

        // For paid/VIP chapters, an authenticated user is required
        if (email == null || email.isBlank()) {
            return false;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }

        // Admin/Staff has privileged access
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
        if (chapterAccessPolicy.isPrivileged(roleNames)) {
            return true;
        }

        return userChapterActivityRepository.findByUserAndChapter(user, chapter)
                .map(act -> Boolean.TRUE.equals(act.getIsUnlocked()))
                .orElse(false);
    }

    /**
     * Returns unlocked chapter IDs for a novel.
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Integer> getUnlockedChapterIds(String email, Integer novelId) {
        if (email == null || email.isBlank() || novelId == null) {
            return Collections.emptySet();
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return Collections.emptySet();
        }

        // Admin/Staff has access to all chapters
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
        if (chapterAccessPolicy.isPrivileged(roleNames)) {
            return chapterRepository.findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId).stream()
                    .map(Chapter::getId)
                    .collect(Collectors.toSet());
        }

        return userChapterActivityRepository.findUnlockedChapterIdsByUserAndNovel(user.getId(), novelId);
    }

    /**
     * Unlocks a chapter and deducts coins from user's wallet.
     */
    @Override
    @Transactional
    public UnlockResultDTO unlockChapter(String email, Integer chapterId, Boolean autoUnlockNext) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("Vui lòng đăng nhập để mở khóa chương");
        }

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương truyện"));

        validateChapterEligibility(chapter);

        // Pessimistic lock on user to prevent double-spending
        User lockedUser = userRepository.findByEmailWithLock(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        // Privileged account (Admin/Staff)
        Set<String> roleNames = lockedUser.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
        if (chapterAccessPolicy.isPrivileged(roleNames)) {
            return buildPrivilegedResult(chapter, lockedUser, autoUnlockNext);
        }

        // Already unlocked
        Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapter(lockedUser,
                chapter);
        if (activityOpt.isPresent() && Boolean.TRUE.equals(activityOpt.get().getIsUnlocked())) {
            return buildAlreadyUnlockedResult(chapter, lockedUser, autoUnlockNext);
        }

        // Free chapter
        if (!chapterAccessPolicy.isPaid(chapter)) {
            return buildFreeResult(chapter, lockedUser, autoUnlockNext, activityOpt);
        }

        // Deduct coins and record unlock activity
        int price = chapter.getPrice();
        String description = buildTransactionDescription(chapter);
        walletService.deductCoins(lockedUser, price, TransactionType.UNLOCK_CHAPTER.name(), chapter.getId(),
                description);

        markChapterUnlocked(lockedUser, chapter, price, activityOpt);
        int remainingBalance = lockedUser.getWalletBalance() != null ? lockedUser.getWalletBalance() : 0;

        log.info("User {} unlocked chapter {} ({} Coin)", email, chapterId, price);

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

    // Validates novel and chapter eligibility
    private void validateChapterEligibility(Chapter chapter) {
        if (Boolean.TRUE.equals(chapter.getIsDeleted())) {
            throw new ResourceNotFoundException("Chương truyện đã bị xóa hoặc không khả dụng");
        }

        Novel novel = chapter.getNovel();
        if (novel == null || Boolean.TRUE.equals(novel.getIsDeleted())) {
            throw new ResourceNotFoundException("Truyện không tồn tại hoặc đã bị xóa");
        }

        if (NovelStatus.ARCHIVED.name().equalsIgnoreCase(novel.getStatus())) {
            throw new ChapterNotPurchasableException("Bộ truyện đang tạm ẩn, không thể mở khóa chương");
        }

        if (ChapterStatus.DRAFT.name().equalsIgnoreCase(chapter.getStatus())
                || ChapterStatus.LOCKED.name().equalsIgnoreCase(chapter.getStatus())) {
            throw new ChapterNotPurchasableException("Chương truyện chưa phát hành hoặc đang bị khóa");
        }
    }

    // Builds wallet transaction description
    private String buildTransactionDescription(Chapter chapter) {
        String novelTitle = chapter.getNovel() != null ? chapter.getNovel().getTitle() : "";
        return String.format("Mở khóa Chương %d: %s (%s)",
                chapter.getChapterNumber(), chapter.getTitle(), novelTitle);
    }

    // Result for privileged account
    private UnlockResultDTO buildPrivilegedResult(Chapter chapter, User user, Boolean autoUnlockNext) {
        int balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        return UnlockResultDTO.builder()
                .success(true)
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .pricePaid(0)
                .remainingBalance(balance)
                .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                .message("Tài khoản quản trị/nhân viên có quyền truy cập chương.")
                .build();
    }

    // Result when already unlocked
    private UnlockResultDTO buildAlreadyUnlockedResult(Chapter chapter, User user, Boolean autoUnlockNext) {
        int balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        return UnlockResultDTO.builder()
                .success(true)
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .pricePaid(0)
                .remainingBalance(balance)
                .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                .message("Bạn đã mở khóa chương này trước đó.")
                .build();
    }

    // Result for free chapter
    private UnlockResultDTO buildFreeResult(Chapter chapter, User user, Boolean autoUnlockNext,
            Optional<UserChapterActivity> existingOpt) {
        markChapterUnlocked(user, chapter, 0, existingOpt);
        int balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        return UnlockResultDTO.builder()
                .success(true)
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .pricePaid(0)
                .remainingBalance(balance)
                .autoUnlockNext(Boolean.TRUE.equals(autoUnlockNext))
                .message("Chương miễn phí, mở khóa thành công!")
                .build();
    }

    // Saves chapter unlock record
    private void markChapterUnlocked(User user, Chapter chapter, int price,
            Optional<UserChapterActivity> existingOpt) {
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
