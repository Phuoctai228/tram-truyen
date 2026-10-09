package com.tramtruyen.service.impl;

import com.tramtruyen.dto.HomePageDTO;
import com.tramtruyen.dto.LatestChapterDTO;
import com.tramtruyen.dto.NovelSummaryDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.HomeService;
import com.tramtruyen.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;
    private final ChapterRepository chapterRepository;
    private final BookshelfRepository bookshelfRepository;

    @Override
    @Transactional(readOnly = true)
    public HomePageDTO getHomePageData() {
        List<Category> categories = categoryRepository.findAll();

        List<NovelSummaryDTO> recentlyUpdated = novelRepository.findTop10ByIsDeletedFalseOrderByUpdatedAtDesc()
                .stream()
                .map(novel -> mapToSummaryDTO(novel, true))
                .collect(Collectors.toList());

        List<NovelSummaryDTO> hotNovels = novelRepository.findTop10ByIsDeletedFalseOrderByViewsDesc()
                .stream()
                .map(novel -> mapToSummaryDTO(novel, false))
                .collect(Collectors.toList());

        List<NovelSummaryDTO> completedNovels = novelRepository.findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc("COMPLETED")
                .stream()
                .map(novel -> mapToSummaryDTO(novel, false))
                .collect(Collectors.toList());

        List<NovelSummaryDTO> recommendedNovels = novelRepository.findTop10ByIsDeletedFalseOrderByAverageRatingDesc()
                .stream()
                .map(novel -> mapToSummaryDTO(novel, true))
                .collect(Collectors.toList());

        return HomePageDTO.builder()
                .categories(categories)
                .recentlyUpdatedNovels(recentlyUpdated)
                .hotNovels(hotNovels)
                .completedNovels(completedNovels)
                .recommendedNovels(recommendedNovels)
                .build();
    }

    private NovelSummaryDTO mapToSummaryDTO(Novel novel, boolean includeChapters) {
        long favoriteCount = bookshelfRepository.countByNovelId(novel.getId());

        List<LatestChapterDTO> latestChapters = null;
        if (includeChapters) {
            latestChapters = chapterRepository
                    .findTop3ByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberDesc(novel.getId(), "PUBLISHED")
                    .stream()
                    .map(ch -> LatestChapterDTO.builder()
                            .id(ch.getId())
                            .chapterNumber(ch.getChapterNumber())
                            .title(ch.getTitle())
                            .updatedAt(ch.getUpdatedAt())
                            .relativeTime(DateTimeUtils.formatRelativeTime(
                                    ch.getUpdatedAt() != null ? ch.getUpdatedAt() : ch.getCreatedAt()))
                            .build())
                    .collect(Collectors.toList());
        }

        return NovelSummaryDTO.builder()
                .id(novel.getId())
                .title(novel.getTitle())
                .slug(novel.getSlug())
                .author(novel.getAuthor())
                .summary(novel.getSummary())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .views(novel.getViews() != null ? novel.getViews() : 0)
                .favoriteCount(favoriteCount)
                .averageRating(novel.getAverageRating())
                .updatedAt(novel.getUpdatedAt())
                .latestChapters(latestChapters)
                .build();
    }
}
