package com.tramtruyen.repository;

import com.tramtruyen.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Persistence access for chapters. */
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {

    boolean existsByNovelIdAndChapterNumber(Integer novelId, Integer chapterNumber);

    boolean existsByNovelIdAndChapterNumberAndIdNot(Integer novelId, Integer chapterNumber, Integer id);

    java.util.Optional<Chapter> findByIdAndNovelIdAndIsDeletedFalse(Integer id, Integer novelId);

    List<Chapter> findAllByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(Integer novelId);

    List<Chapter> findAllByNovelIdAndIsDeletedFalseAndStatusOrderByChapterNumberAsc(Integer novelId, String status);

    java.util.Optional<Chapter> findByNovelIdAndChapterNumberAndIsDeletedFalseAndStatus(Integer novelId, Integer chapterNumber, String status);
}