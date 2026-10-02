package com.tramtruyen.service.impl;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.MediaStorageService;
import com.tramtruyen.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Default business implementation for Novel CMS operations. */
@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

    private static final Set<String> NOVEL_STATUSES = Set.of("ONGOING", "COMPLETED", "ON_HOLD", "ARCHIVED");

    private final NovelRepository novelRepository;
    private final MediaStorageService mediaStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<Novel> findAdminNovels(String query, String status) {
        if (status != null && !status.isBlank() && NOVEL_STATUSES.contains(status)) {
            return novelRepository.findAllByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(status);
        }
        if (query != null && !query.isBlank()) {
            return novelRepository
                    .findAllByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
                            query.trim(), query.trim());
        }
        return novelRepository.findAllByIsDeletedFalseOrderByUpdatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Novel getEditableNovel(Integer id) {
        return findActiveNovel(id);
    }

    @Override
    @Transactional
    public Novel createNovel(NovelForm form) {
        validateStatus(form.getStatus());
        Novel novel = new Novel();
        applyForm(novel, form);
        return novelRepository.save(novel);
    }

    @Override
    @Transactional
    public Novel updateNovel(Integer id, NovelForm form) {
        validateStatus(form.getStatus());
        Novel novel = findActiveNovel(id);
        applyForm(novel, form);
        return novelRepository.save(novel);
    }

    @Override
    @Transactional
    public void deleteNovel(Integer id) {
        Novel novel = findActiveNovel(id);
        novel.setIsDeleted(true);
        novelRepository.save(novel);
    }

    private Novel findActiveNovel(Integer id) {
        return novelRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }

    private void applyForm(Novel novel, NovelForm form) {
        novel.setTitle(form.getTitle().trim());
        novel.setAuthor(form.getAuthor().trim());
        novel.setSummary(form.getSummary().trim());
        novel.setStatus(form.getStatus());

        String coverUrl = mediaStorageService.uploadImage(form.getCover());
        if (coverUrl != null && !coverUrl.isBlank()) {
            novel.setCoverUrl(coverUrl);
        }
    }

    private void validateStatus(String status) {
        if (!NOVEL_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Trạng thái truyện không hợp lệ");
        }
    }
}