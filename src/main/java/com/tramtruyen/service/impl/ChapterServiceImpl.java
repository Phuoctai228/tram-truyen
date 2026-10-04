package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Default business implementation for chapter creation. */
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private static final String DEFAULT_STATUS = "DRAFT";
    private static final int DEFAULT_PRICE = 0;

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> findNovelChapters(Integer novelId) {
        findActiveNovel(novelId);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
    }

    @Override
    @Transactional
    public Chapter createChapter(Integer novelId, ChapterForm form) {
        Novel novel = findActiveNovel(novelId);
        if (chapterRepository.existsByNovelIdAndChapterNumber(novelId, form.getChapterNumber())) {
            throw new DuplicateResourceException("Số chương đã tồn tại trong truyện này");
        }

        Chapter chapter = Chapter.builder()
                .novel(novel)
                .chapterNumber(form.getChapterNumber())
                .title(form.getTitle().trim())
                .content(form.getContent().trim())
                .status(DEFAULT_STATUS)
                .price(DEFAULT_PRICE)
                .build();
        return chapterRepository.save(chapter);
    }

    @Override
    @Transactional(readOnly = true)
    public Chapter getEditableChapter(Integer novelId, Integer chapterId) {
        findActiveNovel(novelId);
        return findActiveChapter(novelId, chapterId);
    }

    @Override
    @Transactional
    public Chapter updateChapter(Integer novelId, Integer chapterId, ChapterForm form) {
        findActiveNovel(novelId);
        Chapter chapter = findActiveChapter(novelId, chapterId);
        if (chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(
                novelId, form.getChapterNumber(), chapterId)) {
            throw new DuplicateResourceException("Số chương đã tồn tại trong truyện này");
        }

        chapter.setChapterNumber(form.getChapterNumber());
        chapter.setTitle(form.getTitle().trim());
        chapter.setContent(form.getContent().trim());
        return chapterRepository.save(chapter);
    }

    @Override
    @Transactional
    public void deleteChapter(Integer novelId, Integer chapterId) {
        findActiveNovel(novelId);
        Chapter chapter = findActiveChapter(novelId, chapterId);
        chapter.setIsDeleted(true);
        chapterRepository.save(chapter);
    }

    @Override
    @Transactional
    public Chapter getPublicChapter(Integer novelId, Integer chapterNumber) {
        Novel novel = findActiveNovel(novelId);
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalseAndStatus(novelId, chapterNumber, "PUBLISHED")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương"));

        // Lượt đọc chỉ tăng khi truy cập vào đọc từng chương
        chapter.setViews((chapter.getViews() != null ? chapter.getViews() : 0) + 1);
        chapterRepository.save(chapter);

        novel.setViews((novel.getViews() != null ? novel.getViews() : 0) + 1);
        novelRepository.save(novel);

        return chapter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> getPublicChapters(Integer novelId) {
        findActiveNovel(novelId);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberAsc(novelId, "PUBLISHED");
    }

    private Chapter findActiveChapter(Integer novelId, Integer chapterId) {
        return chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(chapterId, novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương"));
    }

    private Novel findActiveNovel(Integer novelId) {
        return novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }
}