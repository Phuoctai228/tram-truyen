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
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.entity.enums.NovelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/** Default business implementation for chapter creation. */
@Service
public class ChapterServiceImpl implements ChapterService {

    private static final String ARCHIVED_STATUS = NovelStatus.ARCHIVED.name();
    private static final int DEFAULT_PRICE = 0;
    public static final List<String> PUBLIC_STATUSES = Arrays.asList(
            ChapterStatus.PUBLISHED_REGULAR.name(),
            ChapterStatus.PUBLISHED_VIP.name(),
            "PUBLISHED"
    );

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

        int price = resolvePrice(form);
        Chapter chapter = Chapter.builder()
                .novel(novel)
                .chapterNumber(form.getChapterNumber())
                .title(form.getTitle().trim())
                .content(richTextSanitizer.sanitize(form.getContent()))
                .status(resolveStatus(action, price))
                .price(price)
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
        int price = resolvePrice(form);
        chapter.setStatus(resolveStatus(action, price));
        chapter.setPrice(price);
        Chapter savedChapter = chapterRepository.save(chapter);
        boolean wasDraft = ChapterStatus.DRAFT.name().equalsIgnoreCase(oldStatus);
        if (wasDraft && ChapterSaveAction.PUBLISHED == action && notificationService != null) {
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
    @Transactional(readOnly = true)
    public Chapter getPublicChapter(Integer novelId, Integer chapterNumber) {
        findPublicNovel(novelId);
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalseAndStatusIn(
                        novelId, chapterNumber, PUBLIC_STATUSES)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương"));
        sanitizeChapter(chapter);
        return chapter;
    }

    @Override
    @Transactional
    public void recordView(Integer novelId, Integer chapterId) {
        chapterRepository.findById(chapterId).ifPresent(chapter -> {
            chapter.setViews((chapter.getViews() != null ? chapter.getViews() : 0) + 1);
            chapterRepository.save(chapter);
        });
        novelRepository.findById(novelId).ifPresent(novel -> {
            novel.setViews((novel.getViews() != null ? novel.getViews() : 0) + 1);
            novelRepository.save(novel);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chapter> getPublicChapters(Integer novelId) {
        findPublicNovel(novelId);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalseAndStatusInOrderByChapterNumberAsc(
                novelId, PUBLIC_STATUSES);
    }

    private Chapter findActiveChapter(Integer novelId, Integer chapterId) {
        return chapterRepository.findByIdAndNovelIdAndIsDeletedFalse(chapterId, novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương"));
    }

    private Novel findActiveNovel(Integer novelId) {
        return novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }

    private Novel findPublicNovel(Integer novelId) {
        return novelRepository.findByIdAndIsDeletedFalseAndStatusNot(novelId, ARCHIVED_STATUS)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }

    private Chapter sanitizeChapter(Chapter chapter) {
        chapter.setContent(richTextSanitizer.sanitize(chapter.getContent()));
        return chapter;
    }

    private String resolveStatus(ChapterSaveAction action, int price) {
        if (action != ChapterSaveAction.PUBLISHED) {
            return ChapterStatus.DRAFT.name();
        }
        return price > DEFAULT_PRICE ? ChapterStatus.PUBLISHED_VIP.name() : ChapterStatus.PUBLISHED_REGULAR.name();
    }

    private int resolvePrice(ChapterForm form) {
        Integer price = form.getPrice();
        if (price == null || price < DEFAULT_PRICE) {
            throw new IllegalArgumentException("Giá Coin phải lớn hơn hoặc bằng 0");
        }
        return price;
    }
}