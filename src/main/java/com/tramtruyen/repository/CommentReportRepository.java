package com.tramtruyen.repository;

import com.tramtruyen.entity.CommentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Integer> {

    boolean existsByUserIdAndCommentIdAndStatusIn(Integer userId, Integer commentId, Collection<String> statuses);

    Page<CommentReport> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<CommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
