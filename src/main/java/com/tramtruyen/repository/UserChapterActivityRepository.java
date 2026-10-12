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
}

