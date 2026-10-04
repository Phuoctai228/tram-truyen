package com.tramtruyen.service.impl;

import com.tramtruyen.dto.BookshelfItemDTO;
import com.tramtruyen.dto.BookshelfPageDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Production implementation of BookshelfService coordinating domain repositories
 * and calculating real reading progress without hardcoded values.
 */
@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final CategoryRepository categoryRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Bookshelf> getUserBookshelf(String email) {
        User user = getUserByEmail(email);
        return bookshelfRepository.findAllByUserWithNovel(user);
    }

    @Override
    @Transactional(readOnly = true)
    public BookshelfPageDTO getBookshelfPage(String email, String filter, String query, String sort, int page, int size) {
        User user = getUserByEmail(email);
        List<Bookshelf> bookshelves = bookshelfRepository.findAllByUserWithNovel(user);

        // Convert each bookshelf entry to BookshelfItemDTO with real data
        List<BookshelfItemDTO> allItems = bookshelves.stream()
                .map(b -> mapToBookshelfItem(user, b))
                .collect(Collectors.toList());

        // Calculate statistics before filter
        long totalCount = allItems.size();
        long completedCount = allItems.stream().filter(BookshelfItemDTO::isCompleted).count();
        long readingCount = totalCount - completedCount;

        // 1. Search Query filtering (case-insensitive on Title or Author)
        List<BookshelfItemDTO> processedItems = allItems;
        if (query != null && !query.trim().isEmpty()) {
            String lowerQ = query.trim().toLowerCase();
            processedItems = processedItems.stream()
                    .filter(item -> (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQ))
                            || (item.getAuthor() != null && item.getAuthor().toLowerCase().contains(lowerQ)))
                    .collect(Collectors.toList());
        }

        // 2. Tab Filter (ALL, READING, COMPLETED)
        String currentFilter = (filter == null || filter.trim().isEmpty()) ? "ALL" : filter.trim().toUpperCase();
        if ("READING".equals(currentFilter)) {
            processedItems = processedItems.stream()
                    .filter(item -> !item.isCompleted())
                    .collect(Collectors.toList());
        } else if ("COMPLETED".equals(currentFilter)) {
            processedItems = processedItems.stream()
                    .filter(BookshelfItemDTO::isCompleted)
                    .collect(Collectors.toList());
        } else {
            currentFilter = "ALL";
        }

        // 3. Sorting (recent, name, progress)
        String currentSort = (sort == null || sort.trim().isEmpty()) ? "recent" : sort.trim().toLowerCase();
        if ("name".equals(currentSort)) {
            processedItems.sort(Comparator.comparing(
                    item -> item.getTitle() != null ? item.getTitle().toLowerCase() : ""));
        } else if ("progress".equals(currentSort)) {
            processedItems.sort(Comparator.comparingInt(BookshelfItemDTO::getProgressPercentage).reversed());
        } else {
            currentSort = "recent";
            processedItems.sort((a, b) -> {
                LocalDateTime timeA = a.getAddedAt() != null ? a.getAddedAt() : LocalDateTime.MIN;
                LocalDateTime timeB = b.getAddedAt() != null ? b.getAddedAt() : LocalDateTime.MIN;
                return timeB.compareTo(timeA);
            });
        }

        // 4. Pagination calculation
        int pageSize = size > 0 ? size : 8;
        int totalItems = processedItems.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        int currentPage = Math.max(1, Math.min(page, totalPages));

        int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<BookshelfItemDTO> pagedItems = processedItems.subList(fromIndex, toIndex);

        // Popular categories for empty state
        List<Category> popularCategories = categoryRepository.findAll();

        return BookshelfPageDTO.builder()
                .items(pagedItems)
                .totalCount(totalCount)
                .readingCount(readingCount)
                .completedCount(completedCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .hasPrevious(currentPage > 1)
                .hasNext(currentPage < totalPages)
                .currentFilter(currentFilter)
                .searchQuery(query != null ? query.trim() : "")
                .currentSort(currentSort)
                .popularCategories(popularCategories)
                .build();
    }

    @Override
    @Transactional
    public void addNovelToBookshelf(String email, Integer novelId) {
        User user = getUserByEmail(email);
        Novel novel = getNovelById(novelId);

        if (bookshelfRepository.existsByUserAndNovel(user, novel)) {
            throw new DuplicateResourceException("Truyện đã có trong tủ sách của bạn");
        }

        Bookshelf bookshelf = Bookshelf.builder()
                .user(user)
                .novel(novel)
                .build();

        bookshelfRepository.save(bookshelf);
    }

    @Override
    @Transactional
    public void removeNovelFromBookshelf(String email, Integer novelId) {
        User user = getUserByEmail(email);
        Novel novel = getNovelById(novelId);

        Bookshelf bookshelf = bookshelfRepository.findByUserAndNovel(user, novel)
                .orElseThrow(() -> new ResourceNotFoundException("Truyện không tồn tại trong tủ sách"));

        bookshelfRepository.delete(bookshelf);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNovelInBookshelf(String email, Integer novelId) {
        try {
            User user = getUserByEmail(email);
            Novel novel = getNovelById(novelId);
            return bookshelfRepository.existsByUserAndNovel(user, novel);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countNovelFavorites(Integer novelId) {
        if (novelId == null) {
            return 0L;
        }
        return bookshelfRepository.countByNovelId(novelId);
    }

    @Override
    @Transactional
    public void markNovelAsRead(String email, Integer novelId) {
        User user = getUserByEmail(email);
        Novel novel = getNovelById(novelId);

        List<Chapter> chapters = chapterRepository.findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novel.getId());
        LocalDateTime now = LocalDateTime.now();

        for (Chapter chapter : chapters) {
            Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapter(user, chapter);
            UserChapterActivity activity;
            if (activityOpt.isPresent()) {
                activity = activityOpt.get();
                activity.setIsRead(true);
                activity.setReadAt(now);
            } else {
                activity = UserChapterActivity.builder()
                        .user(user)
                        .chapter(chapter)
                        .isRead(true)
                        .readAt(now)
                        .build();
            }
            userChapterActivityRepository.save(activity);
        }
    }

    @Override
    @Transactional
    public void recordChapterRead(String email, Integer chapterId) {
        try {
            User user = getUserByEmail(email);
            Optional<Chapter> chapterOpt = chapterRepository.findById(chapterId);
            if (chapterOpt.isEmpty()) {
                return;
            }
            Chapter chapter = chapterOpt.get();
            Optional<UserChapterActivity> activityOpt = userChapterActivityRepository.findByUserAndChapter(user, chapter);
            UserChapterActivity activity;
            if (activityOpt.isPresent()) {
                activity = activityOpt.get();
                activity.setIsRead(true);
                activity.setReadAt(LocalDateTime.now());
            } else {
                activity = UserChapterActivity.builder()
                        .user(user)
                        .chapter(chapter)
                        .isRead(true)
                        .readAt(LocalDateTime.now())
                        .build();
            }
            userChapterActivityRepository.save(activity);
        } catch (Exception ignored) {
            // Fail-safe: do not block chapter reading
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String email) {
        User user = getUserByEmail(email);
        return UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .walletBalance(user.getWalletBalance() != null ? user.getWalletBalance() : 0)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private BookshelfItemDTO mapToBookshelfItem(User user, Bookshelf bookshelf) {
        Novel novel = bookshelf.getNovel();

        // Extract real category names
        List<String> categories = new ArrayList<>();
        if (novel.getCategories() != null && !novel.getCategories().isEmpty()) {
            categories = novel.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
        }

        // Total published chapters
        int totalChapters = (int) chapterRepository.countByNovelIdAndIsDeletedFalse(novel.getId());
        boolean hasVip = chapterRepository.existsByNovelIdAndPriceGreaterThanAndIsDeletedFalse(novel.getId(), 0);

        // Reading activity lookup
        Optional<UserChapterActivity> lastActivityOpt =
                userChapterActivityRepository.findFirstByUserAndChapter_NovelAndIsReadTrueOrderByReadAtDescChapter_ChapterNumberDesc(user, novel);
        long readCount = userChapterActivityRepository.countByUserAndChapter_NovelAndIsReadTrue(user, novel);

        int currentChapterNumber = 0;
        LocalDateTime lastTime = bookshelf.getAddedAt();

        if (lastActivityOpt.isPresent()) {
            UserChapterActivity lastActivity = lastActivityOpt.get();
            currentChapterNumber = lastActivity.getChapter().getChapterNumber();
            if (lastActivity.getReadAt() != null) {
                lastTime = lastActivity.getReadAt();
            }
        }

        // Calculate progress percentage
        int progressPercentage = 0;
        boolean isCompleted = false;

        if (totalChapters > 0) {
            if (readCount >= totalChapters || (readCount > 0 && "COMPLETED".equalsIgnoreCase(novel.getStatus()) && readCount >= totalChapters)) {
                progressPercentage = 100;
                isCompleted = true;
                currentChapterNumber = totalChapters;
            } else if (readCount > 0) {
                progressPercentage = (int) Math.min(99, Math.round(((double) readCount / totalChapters) * 100));
            }
        }

        // Determine read URL with slug
        String novelSlug = novel.getSlug();
        String readUrl;
        if (totalChapters == 0) {
            readUrl = "/truyen/" + novelSlug;
        } else if (isCompleted) {
            // Read again from chapter 1
            readUrl = "/truyen/" + novelSlug + "/chuong-1";
        } else if (currentChapterNumber > 0 && currentChapterNumber < totalChapters) {
            readUrl = "/truyen/" + novelSlug + "/chuong-" + (currentChapterNumber + 1);
        } else {
            readUrl = "/truyen/" + novelSlug + "/chuong-1";
        }

        return BookshelfItemDTO.builder()
                .novelId(novel.getId())
                .slug(novelSlug)
                .title(novel.getTitle())
                .author(novel.getAuthor())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .categories(categories)
                .totalChapters(totalChapters)
                .currentChapterNumber(currentChapterNumber)
                .progressPercentage(progressPercentage)
                .isCompleted(isCompleted)
                .hasVipChapters(hasVip)
                .addedAt(bookshelf.getAddedAt())
                .relativeTime(formatRelativeTime(lastTime))
                .readUrl(readUrl)
                .build();
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Gần đây";
        }
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return "Vừa xong";
        }
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " phút trước";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }
        long days = duration.toDays();
        if (days == 1) {
            return "Hôm qua";
        }
        if (days < 30) {
            return days + " ngày trước";
        }
        long months = days / 30;
        if (months < 12) {
            return months + " tháng trước";
        }
        return (months / 12) + " năm trước";
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private Novel getNovelById(Integer id) {
        return novelRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }
}
