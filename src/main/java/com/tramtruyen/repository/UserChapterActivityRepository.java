package com.tramtruyen.repository;

import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence access for user chapter activities and reading progress.
 */
@Repository
public interface UserChapterActivityRepository extends JpaRepository<UserChapterActivity, Integer> {

    Optional<UserChapterActivity> findByUserAndChapter(User user, Chapter chapter);

    Optional<UserChapterActivity> findFirstByUserAndChapter_NovelAndIsReadTrueOrderByReadAtDescChapter_ChapterNumberDesc(
            User user, Novel novel);

    long countByUserAndChapter_NovelAndIsReadTrue(User user, Novel novel);

    Page<UserChapterActivity> findAllByUserAndIsReadTrueOrderByReadAtDesc(User user, Pageable pageable);

    Optional<UserChapterActivity> findByUserAndChapterId(User user, Integer chapterId);

    Optional<UserChapterActivity> findByUserAndChapterIdAndIsUnlockedTrue(User user, Integer chapterId);

    boolean existsByUserAndChapterIdAndIsUnlockedTrue(User user, Integer chapterId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT a.chapter.novel.id AS novelId, COUNT(a.id) AS readCount " +
        "FROM UserChapterActivity a " +
        "WHERE a.isRead = true AND a.readAt >= :since " +
        "GROUP BY a.chapter.novel.id " +
        "ORDER BY COUNT(a.id) DESC"
    )
    List<Object[]> findTopNovelIdsByReadAtSince(@org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);

    @org.springframework.data.jpa.repository.Query(
        "SELECT a.chapter.novel.id AS novelId, COUNT(a.id) AS readCount " +
        "FROM UserChapterActivity a " +
        "JOIN a.chapter.novel.categories c " +
        "WHERE a.isRead = true AND a.readAt >= :since AND c.id = :categoryId " +
        "GROUP BY a.chapter.novel.id " +
        "ORDER BY COUNT(a.id) DESC"
    )
    List<Object[]> findTopNovelIdsByCategoryIdAndReadAtSince(
        @org.springframework.data.repository.query.Param("categoryId") Integer categoryId,
        @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since
    );
}

