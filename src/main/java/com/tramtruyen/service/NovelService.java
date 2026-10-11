package com.tramtruyen.service;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.entity.Novel;

import java.util.List;

/** Business operations for internal novel management. */
public interface NovelService {

    List<Novel> findAdminNovels(String query, String status);

    Novel getEditableNovel(Integer id);
    
    Novel getPublicNovel(Integer id);
    
    Novel getPublicNovelBySlug(String slug);
    
    List<Novel> searchPublicNovels(String query);
    
    org.springframework.data.domain.Page<Novel> advancedSearch(String query, java.util.List<Integer> categoryIds, String status, Integer minChapters, Integer maxChapters, Double minRating, org.springframework.data.domain.Pageable pageable);

    Novel createNovel(NovelForm form);

    Novel updateNovel(Integer id, NovelForm form);

    void deleteNovel(Integer id);

    List<Novel> getNovelsBySameAuthor(String author, Integer currentNovelId);

    List<Novel> getNovelsByCategory(Integer categoryId);
}