package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ReadingHistoryItemDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.ResourceNotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production implementation of ReadingHistoryService respecting BR-14 (50 recent entries limit).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private static final int MAX_HISTORY_LIMIT = 50;

    private final UserRepository userRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingHistoryItemDTO> getReadingHistory(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        int effectiveSize = Math.max(1, Math.min(size > 0 ? size : 10, MAX_HISTORY_LIMIT));
        int effectivePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize);

        Page<UserChapterActivity> activitiesPage =
                userChapterActivityRepository.findAllByUserAndIsReadTrueOrderByReadAtDesc(user, pageable);

        return activitiesPage.map(this::mapToHistoryDTO);
    }

    @Override
    @Transactional
    public void removeHistoryItem(String email, Integer chapterId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapterId(user, chapterId);
        if (activityOpt.isPresent()) {
            UserChapterActivity activity = activityOpt.get();
            // If the user also unlocked this chapter, we only clear read history flag, preserving the purchase
            if (Boolean.TRUE.equals(activity.getIsUnlocked())) {
                activity.setIsRead(false);
                activity.setReadAt(null);
                userChapterActivityRepository.save(activity);
            } else {
                userChapterActivityRepository.delete(activity);
            }
            log.info("Removed chapter {} from reading history for user {}", chapterId, email);
        }
    }

    @Override
    @Transactional
    public void clearHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Pageable allPage = PageRequest.of(0, MAX_HISTORY_LIMIT);
        Page<UserChapterActivity> activities =
                userChapterActivityRepository.findAllByUserAndIsReadTrueOrderByReadAtDesc(user, allPage);

        for (UserChapterActivity activity : activities.getContent()) {
            if (Boolean.TRUE.equals(activity.getIsUnlocked())) {
                activity.setIsRead(false);
                activity.setReadAt(null);
                userChapterActivityRepository.save(activity);
            } else {
                userChapterActivityRepository.delete(activity);
            }
        }
        log.info("Cleared all reading history for user {}", email);
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
