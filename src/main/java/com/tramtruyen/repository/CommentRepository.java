package com.tramtruyen.repository;

import com.tramtruyen.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByChapterIdAndStatusOrderByCreatedAtDesc(Integer chapterId, String status);

    Page<Comment> findByChapterIdAndStatus(Integer chapterId, String status, Pageable pageable);
}
