package com.tramtruyen.service.impl;

import com.tramtruyen.dto.LatestChapterDTO;
import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.dto.NovelSummaryDTO;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.service.MediaStorageService;
import com.tramtruyen.service.NovelService;
import com.tramtruyen.util.DateTimeUtils;
import com.tramtruyen.util.SlugUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Default business implementation for Novel CMS operations. */
@Service
@RequiredArgsConstructor
public class NovelServiceImpl implements NovelService {

    private static final String ARCHIVED_STATUS = "ARCHIVED";
    private static final Set<String> NOVEL_STATUSES = Set.of("ONGOING", "COMPLETED", "ON_HOLD", "ARCHIVED");

    private final NovelRepository novelRepository;
    private final MediaStorageService mediaStorageService;
    private final CategoryRepository categoryRepository;
    private final ChapterRepository chapterRepository;
    private final BookshelfRepository bookshelfRepository;

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
        return novelRepository.findByIdAndIsDeletedFalseAndStatusNot(id, ARCHIVED_STATUS)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));
    }

    @Override
    @Transactional
    public Novel getPublicNovelBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResourceNotFoundException("Slug không hợp lệ");
        }
        return novelRepository.findBySlugAndIsDeletedFalseAndStatusNot(slug.trim(), ARCHIVED_STATUS)
                .orElseGet(() -> {
                    // Try parsing as integer id in case URL is numeric like /truyen/1
                    try {
                        Integer id = Integer.parseInt(slug.trim());
                        return findActiveNovel(id);
                    } catch (NumberFormatException ignored) {}

                    // Fallback: match by generated slug or check all active novels
                    List<Novel> all = getPublicNovels(novelRepository.findAllByIsDeletedFalseOrderByUpdatedAtDesc());
                    for (Novel n : all) {
                        String generatedSlug = SlugUtils.toSlug(n.getTitle());
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
            return Collections.emptyList();
        }
        return novelRepository.findTop5ByAuthorIgnoreCaseAndIdNotAndIsDeletedFalseOrderByViewsDesc(
                        author.trim(), currentNovelId)
                .stream()
                .filter(this::isPublicNovel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Novel> searchPublicNovels(String query) {
        if (query == null || query.isBlank()) {
            return getPublicNovels(novelRepository.findAllByIsDeletedFalseOrderByUpdatedAtDesc());
        }
        return getPublicNovels(novelRepository
                .findAllByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
                        query.trim(), query.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Novel> advancedSearch(String query, List<Integer> categoryIds, String status,
            Integer minChapters, Integer maxChapters, Double minRating, Pageable pageable) {
        Specification<Novel> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
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
            } else {
                predicates.add(cb.notEqual(root.get("status"), ARCHIVED_STATUS));
            }

            if (minRating != null && minRating > 0.0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), BigDecimal.valueOf(minRating)));
            }

            if (minChapters != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("chapterCount"), minChapters));
            }
            if (maxChapters != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("chapterCount"), maxChapters));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                Join<Novel, Category> categoryJoin = root.join("categories");
                predicates.add(categoryJoin.get("id").in(categoryIds));
            }

            // Distinct is needed when joining collections to prevent duplicate rows
            cq.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return novelRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Novel> getNovelsByCategory(Integer categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return novelRepository.findByCategoriesIdAndIsDeletedFalseAndStatusNotOrderByUpdatedAtDesc(
                categoryId, ARCHIVED_STATUS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NovelSummaryDTO> getNovelSummariesByCategory(Integer categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        List<Novel> novels = novelRepository.findByCategoriesIdAndIsDeletedFalseAndStatusNotOrderByUpdatedAtDesc(
                categoryId, ARCHIVED_STATUS);
        return novels.stream()
                .map(this::toNovelSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NovelSummaryDTO> getNovelSummariesByCategoryPage(Integer categoryId, Pageable pageable) {
        if (categoryId == null) {
            return Page.empty(pageable);
        }
        Page<Novel> page = novelRepository
                .findByCategoriesIdAndIsDeletedFalseAndStatusNotOrderByUpdatedAtDesc(
                        categoryId, ARCHIVED_STATUS, pageable);
        return page.map(this::toNovelSummaryDTO);
    }

    private NovelSummaryDTO toNovelSummaryDTO(Novel novel) {
        long favoriteCount = bookshelfRepository.countByNovelId(novel.getId());
        List<LatestChapterDTO> latestChapters = chapterRepository
                .findTop3ByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberDesc(novel.getId(), "PUBLISHED")
                .stream()
                .map(ch -> LatestChapterDTO.builder()
                        .id(ch.getId())
                        .chapterNumber(ch.getChapterNumber())
                        .title(ch.getTitle())
                        .updatedAt(ch.getUpdatedAt())
                        .relativeTime(DateTimeUtils.formatRelativeTime(
                                ch.getUpdatedAt() != null ? ch.getUpdatedAt() : ch.getCreatedAt()))
                        .build())
                .collect(Collectors.toList());

        List<Category> novelCategories = null;
        if (novel.getCategories() != null && !novel.getCategories().isEmpty()) {
            novelCategories = new ArrayList<>(novel.getCategories());
        }

        return NovelSummaryDTO.builder()
                .id(novel.getId())
                .title(novel.getTitle())
                .slug(novel.getSlug())
                .author(novel.getAuthor())
                .summary(novel.getSummary())
                .coverUrl(novel.getCoverUrl())
                .status(novel.getStatus())
                .views(novel.getViews() != null ? novel.getViews() : 0)
                .favoriteCount(favoriteCount)
                .averageRating(novel.getAverageRating())
                .chapterCount(novel.getChapterCount() != null ? novel.getChapterCount() : 0)
                .categories(novelCategories)
                .createdAt(novel.getCreatedAt())
                .updatedAt(novel.getUpdatedAt())
                .latestChapters(latestChapters)
                .build();
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
    public void archiveNovel(Integer id) {
        Novel novel = findActiveNovel(id);
        novel.setStatus(ARCHIVED_STATUS);
        novelRepository.save(novel);
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
            novel.setSlug(SlugUtils.toSlug(form.getTitle().trim()));
        }

        if (form.getCategoryIds() != null && !form.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(form.getCategoryIds()));
            novel.setCategories(categories);
        } else {
            novel.setCategories(new HashSet<>());
        }
    }

    private void validateStatus(String status) {
        if (!NOVEL_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Trạng thái truyện không hợp lệ");
        }
    }

    private List<Novel> getPublicNovels(List<Novel> novels) {
        return novels.stream()
                .filter(this::isPublicNovel)
                .collect(Collectors.toList());
    }

    private boolean isPublicNovel(Novel novel) {
        return !ARCHIVED_STATUS.equals(novel.getStatus());
    }
}