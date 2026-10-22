package com.tramtruyen.repository;

import com.tramtruyen.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Persistence access for chapters. */
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

    boolean existsByNovelIdAndChapterNumber(Integer novelId, Integer chapterNumber);

    boolean existsByNovelIdAndChapterNumberAndIdNot(Integer novelId, Integer chapterNumber, Integer id);

    Optional<Chapter> findByIdAndNovelIdAndIsDeletedFalse(Integer id, Integer novelId);

    List<Chapter> findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(Integer novelId);

    List<Chapter> findAllByNovelIdAndIsDeletedFalseAndStatusInOrderByChapterNumberAsc(
            Integer novelId, List<String> statuses);

    Optional<Chapter> findByNovelIdAndChapterNumberAndIsDeletedFalseAndStatusIn(
            Integer novelId, Integer chapterNumber, List<String> statuses);

    long countByNovelIdAndIsDeletedFalse(Integer novelId);

    boolean existsByNovelIdAndPriceGreaterThanAndIsDeletedFalse(Integer novelId, Integer price);

    java.util.Optional<Chapter> findFirstByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(Integer novelId);

    java.util.Optional<Chapter> findFirstByNovelIdAndIsDeletedFalseOrderByChapterNumberDesc(Integer novelId);

    List<Chapter> findTop3ByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberDesc(Integer novelId, String status);
}