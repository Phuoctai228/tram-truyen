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

    Page<ContentReport> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<ContentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
