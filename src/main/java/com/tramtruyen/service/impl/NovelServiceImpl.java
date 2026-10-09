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

import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.entity.Category;

/** Default business implementation for Novel CMS operations. */
@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

    private static final Set<String> NOVEL_STATUSES = Set.of("ONGOING", "COMPLETED", "ON_HOLD", "ARCHIVED");

    private final NovelRepository novelRepository;
    private final MediaStorageService mediaStorageService;
    private final CategoryRepository categoryRepository;

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
    @Transactional(readOnly = true)
    public Novel getPublicNovel(Integer id) {
        return findActiveNovel(id);
    }

    @Override
    @Transactional
    public Novel getPublicNovelBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResourceNotFoundException("Slug không hợp lệ");
        }
        return novelRepository.findBySlugAndIsDeletedFalse(slug.trim())
                .orElseGet(() -> {
                    // Try parsing as integer id in case URL is numeric like /truyen/1
                    try {
                        Integer id = Integer.parseInt(slug.trim());
                        return findActiveNovel(id);
                    } catch (NumberFormatException ignored) {}

                    // Fallback: match by generated slug or check all active novels
                    List<Novel> all = novelRepository.findAllByIsDeletedFalseOrderByUpdatedAtDesc();
                    for (Novel n : all) {
                        String generatedSlug = com.tramtruyen.util.SlugUtils.toSlug(n.getTitle());
                        if (slug.equalsIgnoreCase(n.getSlug()) || slug.equalsIgnoreCase(generatedSlug)
                                || slug.equalsIgnoreCase(generatedSlug + "-" + n.getId())
                                || slug.equalsIgnoreCase("novel-" + n.getId())) {
                            if (n.getSlug() == null || n.getSlug().isBlank()) {
                                n.setSlug(generatedSlug);
                                novelRepository.save(n);
                            }
                            return n;
                        }
                    }
                    throw new ResourceNotFoundException("Không tìm thấy truyện: " + slug);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Novel> getNovelsBySameAuthor(String author, Integer currentNovelId) {
        if (author == null || author.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return novelRepository.findTop5ByAuthorIgnoreCaseAndIdNotAndIsDeletedFalseOrderByViewsDesc(
                author.trim(), currentNovelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Novel> searchPublicNovels(String query) {
        if (query == null || query.isBlank()) {
            return novelRepository.findAllByIsDeletedFalseOrderByUpdatedAtDesc();
        }
        return novelRepository
                .findAllByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
                        query.trim(), query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Novel> advancedSearch(String query, List<Integer> categoryIds, String status, Integer minChapters, Integer maxChapters, Double minRating, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Novel> spec = (root, cq, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (query != null && !query.isBlank()) {
                String likeQuery = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), likeQuery),
                    cb.like(cb.lower(root.get("author")), likeQuery)
                ));
            }

            if (status != null && !status.isBlank() && NOVEL_STATUSES.contains(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }

            if (minChapters != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("chapterCount"), minChapters));
            }
            if (maxChapters != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("chapterCount"), maxChapters));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                jakarta.persistence.criteria.Join<Novel, Category> categoryJoin = root.join("categories");
                predicates.add(categoryJoin.get("id").in(categoryIds));
            }

            // Distinct is needed when joining collections to prevent duplicate rows
            cq.distinct(true);
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return novelRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Novel> getNovelsByCategory(Integer categoryId) {
        if (categoryId == null) {
            return java.util.Collections.emptyList();
        }
        return novelRepository.findByCategoriesIdAndIsDeletedFalseOrderByUpdatedAtDesc(categoryId);
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

        if (novel.getSlug() == null || novel.getSlug().isBlank()) {
            novel.setSlug(com.tramtruyen.util.SlugUtils.toSlug(form.getTitle().trim()));
        }

        if (form.getCategoryIds() != null && !form.getCategoryIds().isEmpty()) {
            Set<Category> categories = new java.util.HashSet<>(categoryRepository.findAllById(form.getCategoryIds()));
            novel.setCategories(categories);
        } else {
            novel.setCategories(new java.util.HashSet<>());
        }
    }

    private void validateStatus(String status) {
        if (!NOVEL_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Trạng thái truyện không hợp lệ");
        }
    }
}