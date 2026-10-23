package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.dto.ChapterSaveAction;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.ChapterService;
import com.tramtruyen.service.NotificationService;
import com.tramtruyen.util.RichTextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Default business implementation for chapter creation. */
@Service
public class ChapterServiceImpl implements ChapterService {

    private static final String DEFAULT_STATUS = "DRAFT";
    private static final int DEFAULT_PRICE = 0;

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final RichTextSanitizer richTextSanitizer;
    private final NotificationService notificationService;

    @Autowired
    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              NovelRepository novelRepository,
                              RichTextSanitizer richTextSanitizer,
                              NotificationService notificationService) {
        this.chapterRepository = chapterRepository;
        this.novelRepository = novelRepository;
        this.richTextSanitizer = richTextSanitizer;
        this.notificationService = notificationService;
    }

    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              NovelRepository novelRepository,
                              RichTextSanitizer richTextSanitizer) {
        this(chapterRepository, novelRepository, richTextSanitizer, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> findNovelChapters(Integer novelId) {
        findActiveNovel(novelId);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
    }

    @Override
    @Transactional
    public Chapter createChapter(Integer novelId, ChapterForm form) {
        return createChapter(novelId, form, ChapterSaveAction.DRAFT);
    }

    @Override
    @Transactional
    public Chapter createChapter(Integer novelId, ChapterForm form, ChapterSaveAction action) {
        Novel novel = findActiveNovel(novelId);
        if (chapterRepository.existsByNovelIdAndChapterNumber(novelId, form.getChapterNumber())) {
            throw new DuplicateResourceException("Số chương đã tồn tại trong truyện này");
        }

        Chapter chapter = Chapter.builder()
                .novel(novel)
                .chapterNumber(form.getChapterNumber())
                .title(form.getTitle().trim())
                .content(richTextSanitizer.sanitize(form.getContent()))
                .status(resolveStatus(action))
                .price(DEFAULT_PRICE)
                .build();
        Chapter savedChapter = chapterRepository.save(chapter);
        if (ChapterSaveAction.PUBLISHED == action && notificationService != null) {
            notificationService.notifyNewChapterPublished(novelId, savedChapter.getId());
        }
        return savedChapter;
    }

    @Override
    @Transactional(readOnly = true)
    public Chapter getEditableChapter(Integer novelId, Integer chapterId) {
        findActiveNovel(novelId);
        return sanitizeChapter(findActiveChapter(novelId, chapterId));
    }

    @Override
    @Transactional
    public Chapter updateChapter(Integer novelId, Integer chapterId, ChapterForm form) {
        return updateChapter(novelId, chapterId, form, ChapterSaveAction.DRAFT);
    }

    @Override
    @Transactional
    public Chapter updateChapter(Integer novelId, Integer chapterId, ChapterForm form, ChapterSaveAction action) {
        findActiveNovel(novelId);
        Chapter chapter = findActiveChapter(novelId, chapterId);
        String oldStatus = chapter.getStatus();
        if (chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(
                novelId, form.getChapterNumber(), chapterId)) {
            throw new DuplicateResourceException("Số chương đã tồn tại trong truyện này");
        }

        chapter.setChapterNumber(form.getChapterNumber());
        chapter.setTitle(form.getTitle().trim());
        chapter.setContent(richTextSanitizer.sanitize(form.getContent()));
        chapter.setStatus(resolveStatus(action));
        Chapter savedChapter = chapterRepository.save(chapter);
        if ("DRAFT".equalsIgnoreCase(oldStatus) && ChapterSaveAction.PUBLISHED == action && notificationService != null) {
            notificationService.notifyNewChapterPublished(novelId, savedChapter.getId());
        }
        return savedChapter;
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
        sanitizeChapter(chapter);

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

    private Chapter sanitizeChapter(Chapter chapter) {
        chapter.setContent(richTextSanitizer.sanitize(chapter.getContent()));
        return chapter;
    }

    private String resolveStatus(ChapterSaveAction action) {
        return action == ChapterSaveAction.PUBLISHED ? "PUBLISHED" : DEFAULT_STATUS;
    }
}