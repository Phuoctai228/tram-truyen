package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ReadingHistoryItemDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.ReadingHistoryService;
import com.tramtruyen.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production implementation of ReadingHistoryService respecting BR-14 (50 recent entries limit).
 * <p>
 * Reading history is driven by {@code readAt} (last access time of a chapter), while {@code isRead}
 * keeps its meaning of "chapter completed" used by the progress bar and bookshelf.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private static final int MAX_HISTORY_LIMIT = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String USER_NOT_FOUND_MESSAGE = "Không tìm thấy người dùng";
    private static final String CHAPTER_NOT_FOUND_MESSAGE = "Không tìm thấy chương truyện";

    private final UserRepository userRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;
    private final ChapterRepository chapterRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingHistoryItemDTO> getReadingHistory(String email, int page, int size) {
        User user = getUserByEmail(email);

        int requestedSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        int effectiveSize = Math.max(1, Math.min(requestedSize, MAX_HISTORY_LIMIT));
        int effectivePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize);

        Page<UserChapterActivity> activitiesPage =
                userChapterActivityRepository.findAllByUserAndReadAtIsNotNullOrderByReadAtDesc(user, pageable);

        return activitiesPage.map(this::mapToHistoryDTO);
    }

    @Override
    @Transactional
    public void recordChapterAccess(String email, Integer chapterId) {
        User user = getUserByEmail(email);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND_MESSAGE));

        UserChapterActivity activity = userChapterActivityRepository.findByUserAndChapter(user, chapter)
                .orElseGet(() -> UserChapterActivity.builder()
                        .user(user)
                        .chapter(chapter)
                        .build());
        activity.setReadAt(LocalDateTime.now());
        userChapterActivityRepository.save(activity);
    }

    @Override
    @Transactional
    public void removeHistoryItem(String email, Integer chapterId) {
        User user = getUserByEmail(email);

        Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapterId(user, chapterId);
        if (activityOpt.isPresent()) {
            removeFromHistory(activityOpt.get());
            log.info("Removed chapter {} from reading history for user {}", chapterId, email);
        }
    }

    @Override
    @Transactional
    public void clearHistory(String email) {
        User user = getUserByEmail(email);

        Page<UserChapterActivity> activities =
                userChapterActivityRepository.findAllByUserAndReadAtIsNotNullOrderByReadAtDesc(
                        user, Pageable.unpaged());

        for (UserChapterActivity activity : activities.getContent()) {
            removeFromHistory(activity);
        }
        log.info("Cleared all reading history for user {}", email);
    }

    /**
     * Removes an activity from reading history. Unlocked VIP chapters keep their ownership record
     * (only read flags are reset); other records are deleted.
     *
     * @param activity activity to remove from history
     */
    private void removeFromHistory(UserChapterActivity activity) {
        if (Boolean.TRUE.equals(activity.getIsUnlocked())) {
            activity.setIsRead(false);
            activity.setReadAt(null);
            userChapterActivityRepository.save(activity);
        } else {
            userChapterActivityRepository.delete(activity);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    private ReadingHistoryItemDTO mapToHistoryDTO(UserChapterActivity activity) {
        Chapter chapter = activity.getChapter();
        Novel novel = chapter.getNovel();

        List<String> categories = Collections.emptyList();
        if (novel.getCategories() != null && !novel.getCategories().isEmpty()) {
            categories = novel.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
        }

        boolean isVip = chapter.getPrice() != null && chapter.getPrice() > 0;
        String readUrl = "/truyen/" + novel.getSlug() + "/chuong-" + chapter.getChapterNumber();

        return ReadingHistoryItemDTO.builder()
                .activityId(activity.getId())
                .novelId(novel.getId())
                .novelSlug(novel.getSlug())
                .novelTitle(novel.getTitle())
                .novelCoverUrl(novel.getCoverUrl())
                .author(novel.getAuthor())
                .categories(categories)
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .isVip(isVip)
                .price(chapter.getPrice() != null ? chapter.getPrice() : 0)
                .readAt(activity.getReadAt())
                .relativeTime(DateTimeUtils.formatRelativeTime(activity.getReadAt()))
                .readUrl(readUrl)
                .build();
    }
}
