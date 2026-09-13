package com.tramtruyen.service.impl;

import com.tramtruyen.dto.HomePageDTO;
import com.tramtruyen.dto.NovelSummaryDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final NovelRepository novelRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public HomePageDTO getHomePageData() {
        List<Category> categories = categoryRepository.findAll();

        List<NovelSummaryDTO> recentlyUpdated = novelRepository.findTop10ByIsDeletedFalseOrderByUpdatedAtDesc()
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());

        List<NovelSummaryDTO> hotNovels = novelRepository.findTop10ByIsDeletedFalseOrderByViewsDesc()
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());

        List<NovelSummaryDTO> completedNovels = novelRepository.findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc("COMPLETED")
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());

        return HomePageDTO.builder()
                .categories(categories)
                .recentlyUpdatedNovels(recentlyUpdated)
                .hotNovels(hotNovels)
                .completedNovels(completedNovels)
                .build();
    }

    private NovelSummaryDTO mapToSummaryDTO(Novel novel) {
        return NovelSummaryDTO.builder()
                .id(novel.getId())
                .title(novel.getTitle())
                .author(novel.getAuthor())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .views(novel.getViews())
                .averageRating(novel.getAverageRating())
                .updatedAt(novel.getUpdatedAt())
                .build();
    }
}
