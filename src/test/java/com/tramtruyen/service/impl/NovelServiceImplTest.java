package com.tramtruyen.service.impl;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.MediaStorageService;
import com.tramtruyen.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NovelServiceImplTest {

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private CategoryRepository categoryRepository;

    private NovelServiceImpl novelService;

    @BeforeEach
    void setUp() {
        novelService = new NovelServiceImpl(novelRepository, mediaStorageService, categoryRepository);
    }

    @Test
    void createNovelMapsFormAndPersistsNovel() {
        NovelForm form = form();
        when(novelRepository.save(any(Novel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Novel result = novelService.createNovel(form);

        assertThat(result.getTitle()).isEqualTo("  Novel title  ".trim());
        assertThat(result.getStatus()).isEqualTo("ONGOING");
        verify(novelRepository).save(any(Novel.class));
    }

    @Test
    void updateNovelKeepsExistingCoverWhenNoReplacementIsUploaded() {
        Novel existing = Novel.builder().id(7).title("Old").coverUrl("old-cover").isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(7)).thenReturn(Optional.of(existing));
        when(novelRepository.save(any(Novel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Novel result = novelService.updateNovel(7, form());

        assertThat(result.getTitle()).isEqualTo("Novel title");
        assertThat(result.getCoverUrl()).isEqualTo("old-cover");
    }

    @Test
    void deleteNovelMarksRecordDeletedInsteadOfRemovingIt() {
        Novel existing = Novel.builder().id(7).isDeleted(false).build();
        when(novelRepository.findByIdAndIsDeletedFalse(7)).thenReturn(Optional.of(existing));

        novelService.deleteNovel(7);

        assertThat(existing.getIsDeleted()).isTrue();
        verify(novelRepository).save(existing);
    }

    private NovelForm form() {
        NovelForm form = new NovelForm();
        form.setTitle("  Novel title  ");
        form.setAuthor("Author");
        form.setSummary("Summary");
        form.setStatus("ONGOING");
        return form;
    }
}