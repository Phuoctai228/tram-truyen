package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceImplTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private NovelRepository novelRepository;

    private ChapterServiceImpl chapterService;

    @BeforeEach
    void setUp() {
        chapterService = new ChapterServiceImpl(chapterRepository, novelRepository);
    }

    @Test
    void createChapterUsesNovelAndDraftDefaults() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = chapterService.createChapter(1, form());

        assertThat(result.getNovel()).isSameAs(novel);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getPrice()).isZero();
        verify(chapterRepository).save(any());
    }

    @Test
    void createChapterRejectsMissingNovel() {
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterService.createChapter(1, form()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(chapterRepository, never()).save(any());
    }

    @Test
    void createChapterRejectsDuplicateNumber() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(true);

        assertThatThrownBy(() -> chapterService.createChapter(1, form()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(chapterRepository, never()).save(any());
    }

    @Test
    void updateChapterChangesEditableFieldsAndKeepsDraftSettings() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        var chapter = com.tramtruyen.entity.Chapter.builder()
                .id(5)
                .novel(novel)
                .chapterNumber(1)
                .title("Old title")
                .content("Old content")
                .status("DRAFT")
                .price(0)
                .isDeleted(false)
                .build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(5, 1)).thenReturn(Optional.of(chapter));
        when(chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(1, 2, 5)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = chapterService.updateChapter(1, 5, updatedForm());

        assertThat(result.getChapterNumber()).isEqualTo(2);
        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getPrice()).isZero();
    }

    @Test
    void deleteChapterSoftDeletesOwnedChapter() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        var chapter = com.tramtruyen.entity.Chapter.builder()
                .id(5).novel(novel).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(5, 1)).thenReturn(Optional.of(chapter));

        chapterService.deleteChapter(1, 5);

        assertThat(chapter.getIsDeleted()).isTrue();
        verify(chapterRepository).save(chapter);
    }

    private ChapterForm form() {
        ChapterForm form = new ChapterForm();
        form.setChapterNumber(1);
        form.setTitle("Chapter one");
        form.setContent("Content");
        return form;
    }

    private ChapterForm updatedForm() {
        ChapterForm form = new ChapterForm();
        form.setChapterNumber(2);
        form.setTitle("Updated title");
        form.setContent("Updated content");
        return form;
    }
}