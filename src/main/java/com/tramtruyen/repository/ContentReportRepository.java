package com.tramtruyen.repository;

import com.tramtruyen.entity.ContentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, Integer> {

    boolean existsByUserIdAndChapterIdAndStatusIn(Integer userId, Integer chapterId, Collection<String> statuses);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT cr FROM ContentReport cr " +
                    "LEFT JOIN FETCH cr.user " +
                    "LEFT JOIN FETCH cr.chapter ch " +
                    "LEFT JOIN FETCH ch.novel " +
                    "WHERE cr.status = :status ORDER BY cr.createdAt DESC",
            countQuery = "SELECT COUNT(cr) FROM ContentReport cr WHERE cr.status = :status"
    )
    Page<ContentReport> findByStatusOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("status") String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT cr FROM ContentReport cr " +
                    "LEFT JOIN FETCH cr.user " +
                    "LEFT JOIN FETCH cr.chapter ch " +
                    "LEFT JOIN FETCH ch.novel " +
                    "ORDER BY cr.createdAt DESC",
            countQuery = "SELECT COUNT(cr) FROM ContentReport cr"
    )
    Page<ContentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
