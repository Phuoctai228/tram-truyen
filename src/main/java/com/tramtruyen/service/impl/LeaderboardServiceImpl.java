package com.tramtruyen.service.impl;

import com.tramtruyen.dto.LeaderboardDTO;
import com.tramtruyen.dto.LeaderboardItemDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementation for compiling and delivering real leaderboard data
 * based on reading activity timestamps in user_chapter_activities and total novel views.
 */
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;
    private final UserChapterActivityRepository userChapterActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public LeaderboardDTO getLeaderboard(Integer categoryId) {
        return getLeaderboard(categoryId, "all");
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderboardDTO getLeaderboard(Integer categoryId, String timeframe) {
        String activeTimeframe = (timeframe != null && !timeframe.isBlank()) ? timeframe.toLowerCase() : "all";

        List<Novel> allTimeNovels;
        if (categoryId != null && categoryId > 0) {
            allTimeNovels = publicNovels(
                    novelRepository.findByCategoriesIdAndIsDeletedFalseOrderByViewsDesc(categoryId));
        } else {
            allTimeNovels = publicNovels(novelRepository.findAllByIsDeletedFalseOrderByViewsDesc());
        }

        List<NovelRankData> rankedNovels = resolveRankedNovels(categoryId, activeTimeframe, allTimeNovels);
        List<Category> allCategories = categoryRepository.findAll();
        List<LeaderboardItemDTO> items = buildLeaderboardItems(rankedNovels, 10);

        return LeaderboardDTO.builder()
                .currentTimeframe(activeTimeframe)
                .currentCategoryId(categoryId)
                .categories(allCategories)
                .items(items)
                .totalCount(items.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardItemDTO> getLeaderboardItems(String timeframe, int limit) {
        String activeTimeframe = (timeframe != null && !timeframe.isBlank()) ? timeframe.toLowerCase() : "all";
        List<Novel> allTimeNovels = publicNovels(novelRepository.findAllByIsDeletedFalseOrderByViewsDesc());

        List<NovelRankData> rankedNovels = resolveRankedNovels(null, activeTimeframe, allTimeNovels);
        return buildLeaderboardItems(rankedNovels, limit);
    }

    /**
     * Gán thứ hạng chính xác theo quy tắc đồng hạng (cùng views và cùng averageRating)
     */
    private List<LeaderboardItemDTO> buildLeaderboardItems(List<NovelRankData> rankedNovels, int limit) {
        List<LeaderboardItemDTO> result = new ArrayList<>();
        int max = limit > 0 ? Math.min(rankedNovels.size(), limit) : rankedNovels.size();
        int currentRank = 1;

        for (int i = 0; i < max; i++) {
            NovelRankData data = rankedNovels.get(i);
            Novel n = data.novel;

            if (i > 0) {
                NovelRankData prev = rankedNovels.get(i - 1);
                boolean sameViews = (data.views == prev.views);
                BigDecimal currRating = (n.getAverageRating() != null ? n.getAverageRating() : BigDecimal.ZERO);
                BigDecimal prevRating = (prev.novel.getAverageRating() != null ? prev.novel.getAverageRating() : BigDecimal.ZERO);
                boolean sameRating = (currRating.compareTo(prevRating) == 0);

                if (!sameViews || !sameRating) {
                    currentRank = i + 1;
                }
            } else {
                currentRank = 1;
            }

            result.add(toItemDTO(n, currentRank, data.views));
        }
        return result;
    }

    private static class NovelRankData {
        Novel novel;
        int views;
        NovelRankData(Novel novel, int views) {
            this.novel = novel;
            this.views = views;
        }
    }

    private List<NovelRankData> resolveRankedNovels(Integer categoryId, String timeframe, List<Novel> allTimeNovels) {
        if ("all".equals(timeframe)) {
            List<NovelRankData> list = new ArrayList<>();
            for (Novel n : allTimeNovels) {
                int views = n.getViews() != null ? n.getViews() : 0;
                if (views > 0) {
                    list.add(new NovelRankData(n, views));
                }
            }
            // Sắp xếp: lượt xem giảm dần -> nếu bằng nhau, xét average_rating giảm dần
            list.sort((a, b) -> {
                int cmp = Integer.compare(b.views, a.views);
                if (cmp != 0) {
                    return cmp;
                }
                BigDecimal aRating = a.novel.getAverageRating() != null ? a.novel.getAverageRating() : BigDecimal.ZERO;
                BigDecimal bRating = b.novel.getAverageRating() != null ? b.novel.getAverageRating() : BigDecimal.ZERO;
                return bRating.compareTo(aRating);
            });
            return list;
        }

        LocalDateTime since;
        switch (timeframe) {
            case "day":
                since = LocalDateTime.now().minusDays(1);
                break;
            case "week":
                since = LocalDateTime.now().minusDays(7);
                break;
            case "month":
                since = LocalDateTime.now().minusDays(30);
                break;
            default:
                since = LocalDateTime.now().minusDays(7);
                break;
        }

        List<Object[]> rows;
        if (categoryId != null && categoryId > 0) {
            rows = userChapterActivityRepository.findTopNovelIdsByCategoryIdAndReadAtSince(categoryId, since);
        } else {
            rows = userChapterActivityRepository.findTopNovelIdsByReadAtSince(since);
        }

        Map<Integer, Integer> activityCountMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer novelId = (Integer) row[0];
            Long count = (Long) row[1];
            activityCountMap.put(novelId, count != null ? count.intValue() : 0);
        }

        Map<Integer, Novel> novelMap = new HashMap<>();
        for (Novel n : allTimeNovels) {
            novelMap.put(n.getId(), n);
        }

        List<NovelRankData> list = new ArrayList<>();
        // Chỉ thêm những truyện có lượt xem thật trong timeframe (> 0)
        for (Map.Entry<Integer, Integer> entry : activityCountMap.entrySet()) {
            if (entry.getValue() > 0) {
                Novel n = novelMap.get(entry.getKey());
                if (n != null) {
                    list.add(new NovelRankData(n, entry.getValue()));
                }
            }
        }

        // Sắp xếp:
        // 1. Lượt xem timeframe giảm dần
        // 2. Nếu lượt xem timeframe bằng nhau -> xét average_rating (điểm sao) giảm dần
        // 3. Nếu average_rating vẫn bằng nhau -> đồng hạng (giữ cùng thứ bậc khi gán rank)
        list.sort((a, b) -> {
            int cmp = Integer.compare(b.views, a.views);
            if (cmp != 0) {
                return cmp;
            }
            BigDecimal aRating = a.novel.getAverageRating() != null ? a.novel.getAverageRating() : BigDecimal.ZERO;
            BigDecimal bRating = b.novel.getAverageRating() != null ? b.novel.getAverageRating() : BigDecimal.ZERO;
            int ratingCmp = bRating.compareTo(aRating);
            if (ratingCmp != 0) {
                return ratingCmp;
            }
            int aAllTime = a.novel.getViews() != null ? a.novel.getViews() : 0;
            int bAllTime = b.novel.getViews() != null ? b.novel.getViews() : 0;
            return Integer.compare(bAllTime, aAllTime);
        });
        return list;
    }

    private LeaderboardItemDTO toItemDTO(Novel novel, int currentRank, int displayViews) {

        String primaryCategory = "Truyện chữ";
        if (novel.getCategories() != null && !novel.getCategories().isEmpty()) {
            primaryCategory = novel.getCategories().iterator().next().getName();
        }

        return LeaderboardItemDTO.builder()
                .rank(currentRank)
                .formattedRank(currentRank < 10 ? "0" + currentRank : String.valueOf(currentRank))
                .novelId(novel.getId())
                .title(novel.getTitle())
                .slug(novel.getSlug())
                .author(novel.getAuthor())
                .summary(novel.getSummary())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .views(displayViews)
                .formattedViews(formatCount(displayViews))
                .averageRating(novel.getAverageRating())
                .chapterCount(novel.getChapterCount() != null ? novel.getChapterCount() : 0)
                .primaryCategory(primaryCategory)
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

    private List<Novel> publicNovels(List<Novel> novels) {
        return novels.stream()
                .filter(novel -> !"ARCHIVED".equals(novel.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
}
