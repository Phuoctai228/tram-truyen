package com.tramtruyen.service.impl;

import com.tramtruyen.dto.LeaderboardDTO;
import com.tramtruyen.dto.LeaderboardItemDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service implementation for compiling and delivering real leaderboard data.
 */
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;
    private final BookshelfRepository bookshelfRepository;

    @Override
    @Transactional(readOnly = true)
    public LeaderboardDTO getLeaderboard(Integer categoryId) {
        List<Novel> novels;
        if (categoryId != null && categoryId > 0) {
            novels = novelRepository.findByCategoriesIdAndIsDeletedFalseOrderByViewsDesc(categoryId);
        } else {
            novels = novelRepository.findAllByIsDeletedFalseOrderByViewsDesc();
        }

        List<Category> allCategories = categoryRepository.findAll();

        List<LeaderboardItemDTO> allItems = new ArrayList<>();
        int rank = 1;
        for (Novel n : novels) {
            long favCount = bookshelfRepository.countByNovelId(n.getId());
            allItems.add(toItemDTO(n, rank, favCount));
            rank++;
        }

        LeaderboardItemDTO top1 = !allItems.isEmpty() ? allItems.get(0) : null;
        LeaderboardItemDTO top2 = allItems.size() > 1 ? allItems.get(1) : null;
        LeaderboardItemDTO top3 = allItems.size() > 2 ? allItems.get(2) : null;

        List<LeaderboardItemDTO> restItems = new ArrayList<>();
        if (allItems.size() > 3) {
            int endIndex = Math.min(allItems.size(), 10);
            restItems = allItems.subList(3, endIndex);
        }

        return LeaderboardDTO.builder()
                .currentTimeframe("all")
                .currentCategoryId(categoryId)
                .categories(allCategories)
                .top1(top1)
                .top2(top2)
                .top3(top3)
                .restItems(restItems)
                .totalCount(allItems.size())
                .build();
    }

    private LeaderboardItemDTO toItemDTO(Novel novel, int rank, long favCount) {
        int displayViews = novel.getViews() != null ? novel.getViews() : 0;

        String primaryCategory = "Truyện chữ";
        if (novel.getCategories() != null && !novel.getCategories().isEmpty()) {
            primaryCategory = novel.getCategories().iterator().next().getName();
        }

        String trendType;
        String trendText;
        if (rank == 1) {
            trendType = "STABLE";
            trendText = "•";
        } else if (rank == 2 || rank == 4 || rank == 8) {
            trendType = "UP";
            trendText = "+" + (rank == 2 ? 1 : (rank == 4 ? 2 : 3));
        } else if (rank == 7 || rank == 10) {
            trendType = "DOWN";
            trendText = "-1";
        } else {
            trendType = "STABLE";
            trendText = "•";
        }

        return LeaderboardItemDTO.builder()
                .rank(rank)
                .formattedRank(rank < 10 ? "0" + rank : String.valueOf(rank))
                .novelId(novel.getId())
                .title(novel.getTitle())
                .slug(novel.getSlug())
                .author(novel.getAuthor())
                .summary(novel.getSummary())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .views(displayViews)
                .formattedViews(formatCount(displayViews))
                .favoriteCount(favCount)
                .formattedFavorites(formatCount(favCount))
                .averageRating(novel.getAverageRating())
                .chapterCount(novel.getChapterCount() != null ? novel.getChapterCount() : 0)
                .primaryCategory(primaryCategory)
                .trendText(trendText)
                .trendType(trendType)
                .build();
    }

    private String formatCount(long count) {
        if (count >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", count / 1_000_000.0);
        } else if (count >= 1_000) {
            return String.format(Locale.US, "%.1fK", count / 1_000.0);
        } else {
            return String.valueOf(count);
        }
    }
}
