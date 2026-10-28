package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.dto.ChapterSaveAction;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.util.RichTextSanitizer;
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

    private RichTextSanitizer richTextSanitizer;

    private ChapterServiceImpl chapterService;

    @BeforeEach
    void setUp() {
        richTextSanitizer = new RichTextSanitizer();
        chapterService = new ChapterServiceImpl(chapterRepository, novelRepository, richTextSanitizer);
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
    void createChapterCanBePublished() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = chapterService.createChapter(1, form(), ChapterSaveAction.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(ChapterStatus.PUBLISHED_REGULAR.name());
    }

    @Test
    void createPublishedPaidChapterUsesVipStatus() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterForm chapterForm = form();
        chapterForm.setPrice(25);

        var result = chapterService.createChapter(1, chapterForm, ChapterSaveAction.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(ChapterStatus.PUBLISHED_VIP.name());
    }

    @Test
    void createDraftPaidChapterRemainsDraft() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterForm chapterForm = form();
        chapterForm.setPrice(25);

        var result = chapterService.createChapter(1, chapterForm, ChapterSaveAction.DRAFT);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void createChapterStoresPositivePriceForVipChapter() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterForm chapterForm = form();
        chapterForm.setPrice(25);

        var result = chapterService.createChapter(1, chapterForm);

        assertThat(result.getPrice()).isEqualTo(25);
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
    void updateChapterCanBePublished() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        var chapter = com.tramtruyen.entity.Chapter.builder()
                .id(5).novel(novel).chapterNumber(1).title("Old title")
                .content("Old content").status("DRAFT").price(0).isDeleted(false).build();
        when(chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(5, 1)).thenReturn(Optional.of(chapter));
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(1, 2, 5)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = chapterService.updateChapter(1, 5, updatedForm(), ChapterSaveAction.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(ChapterStatus.PUBLISHED_REGULAR.name());
    }

    @Test
    void updateChapterChangesVipPrice() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        var chapter = com.tramtruyen.entity.Chapter.builder()
                .id(5).novel(novel).chapterNumber(1).title("Old title")
                .content("Old content").status("DRAFT").price(0).isDeleted(false).build();
        when(chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(5, 1)).thenReturn(Optional.of(chapter));
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(1, 2, 5)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterForm chapterForm = updatedForm();
        chapterForm.setPrice(40);

        var result = chapterService.updateChapter(1, 5, chapterForm);

        assertThat(result.getPrice()).isEqualTo(40);
    }

    @Test
    void createChapterRejectsNegativePrice() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);

        ChapterForm chapterForm = form();
        chapterForm.setPrice(-1);

        assertThatThrownBy(() -> chapterService.createChapter(1, chapterForm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Giá Coin phải lớn hơn hoặc bằng 0");
        verify(chapterRepository, never()).save(any());
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

    @Test
    void createChapterSanitizesRichText() {
        Novel novel = Novel.builder().id(1).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(1)).thenReturn(Optional.of(novel));
        when(chapterRepository.existsByNovelIdAndChapterNumber(1, 1)).thenReturn(false);
        when(chapterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChapterForm chapterForm = form();
        chapterForm.setContent("<p>Safe</p><script>alert('x')</script>");

        var result = chapterService.createChapter(1, chapterForm);

        assertThat(result.getContent()).isEqualTo("<p>Safe</p>");
    }

    @Test
    void getPublicChapterReturnsChapterWithoutSideEffects() {
        Novel novel = Novel.builder().id(1).status("ONGOING").isDeleted(false).build();
        com.tramtruyen.entity.Chapter chapter = com.tramtruyen.entity.Chapter.builder()
                .id(10).novel(novel).chapterNumber(1).content("Test content").views(5).build();

        when(novelRepository.findByIdAndIsDeletedFalseAndStatusNot(1, "ARCHIVED")).thenReturn(Optional.of(novel));
        when(chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalseAndStatusIn(
                1, 1, ChapterServiceImpl.PUBLIC_STATUSES)).thenReturn(Optional.of(chapter));

        var result = chapterService.getPublicChapter(1, 1);

        assertThat(result).isNotNull();
        assertThat(result.getViews()).isEqualTo(5);
        verify(chapterRepository, never()).save(any());
        verify(novelRepository, never()).save(any());
    }

    @Test
    void recordViewIncrementsChapterAndNovelViews() {
        Novel novel = Novel.builder().id(1).views(10).build();
        com.tramtruyen.entity.Chapter chapter = com.tramtruyen.entity.Chapter.builder().id(10).views(5).build();

        when(chapterRepository.findById(10)).thenReturn(Optional.of(chapter));
        when(novelRepository.findById(1)).thenReturn(Optional.of(novel));

        chapterService.recordView(1, 10);

        assertThat(chapter.getViews()).isEqualTo(6);
        assertThat(novel.getViews()).isEqualTo(11);
        verify(chapterRepository).save(chapter);
        verify(novelRepository).save(novel);
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