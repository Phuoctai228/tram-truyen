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

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT cr FROM CommentReport cr " +
                    "LEFT JOIN FETCH cr.user " +
                    "LEFT JOIN FETCH cr.comment c " +
                    "LEFT JOIN FETCH c.user " +
                    "WHERE cr.status = :status ORDER BY cr.createdAt DESC",
            countQuery = "SELECT COUNT(cr) FROM CommentReport cr WHERE cr.status = :status"
    )
    Page<CommentReport> findByStatusOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("status") String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT cr FROM CommentReport cr " +
                    "LEFT JOIN FETCH cr.user " +
                    "LEFT JOIN FETCH cr.comment c " +
                    "LEFT JOIN FETCH c.user " +
                    "ORDER BY cr.createdAt DESC",
            countQuery = "SELECT COUNT(cr) FROM CommentReport cr"
    )
    Page<CommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
