package com.tramtruyen.service.impl;

import com.tramtruyen.dto.HomePageDTO;
import com.tramtruyen.dto.NovelSummaryDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private BookshelfRepository bookshelfRepository;

    @InjectMocks
    private HomeServiceImpl homeService;

    private Novel sampleNovel;
    private Chapter sampleChapter;

    @BeforeEach
    void setUp() {
        sampleNovel = Novel.builder()
                .id(1)
                .title("Võ Đạo Đan Thần")
                .slug("vo-dao-dan-than")
                .author("Tiêu Dao Du")
                .summary("Tóm tắt")
                .coverUrl("/images/sample.jpg")
                .status("ONGOING")
                .views(1200)
                .build();

        sampleChapter = Chapter.builder()
                .id(10)
                .chapterNumber(5)
                .title("Khởi Đầu Gian Nan")
                .updatedAt(LocalDateTime.now().minusMinutes(10))
                .status("PUBLISHED")
                .build();
    }

    @Test
    void getHomePageDataLoadsLatestChaptersAndFavoriteCountForRecentlyUpdated() {
        when(categoryRepository.findAll()).thenReturn(List.of(Category.builder().name("Tiên Hiệp").build()));
        when(novelRepository.findTop10ByIsDeletedFalseOrderByUpdatedAtDesc()).thenReturn(List.of(sampleNovel));
        when(novelRepository.findTop10ByIsDeletedFalseOrderByViewsDesc()).thenReturn(List.of(sampleNovel));
        when(novelRepository.findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc("COMPLETED")).thenReturn(List.of());

        when(bookshelfRepository.countByNovelId(anyInt())).thenReturn(25L);
        when(chapterRepository.findTop3ByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberDesc(eq(1), eq("PUBLISHED")))
                .thenReturn(List.of(sampleChapter));

        HomePageDTO data = homeService.getHomePageData();

        assertThat(data).isNotNull();
        assertThat(data.getRecentlyUpdatedNovels()).hasSize(1);
        NovelSummaryDTO novelDTO = data.getRecentlyUpdatedNovels().get(0);
        assertThat(novelDTO.getTitle()).isEqualTo("Võ Đạo Đan Thần");
        assertThat(novelDTO.getFavoriteCount()).isEqualTo(25L);
        assertThat(novelDTO.getLatestChapters()).hasSize(1);
        assertThat(novelDTO.getLatestChapters().get(0).getChapterNumber()).isEqualTo(5);
        assertThat(novelDTO.getLatestChapters().get(0).getRelativeTime()).isEqualTo("10 phút trước");
    }
}
