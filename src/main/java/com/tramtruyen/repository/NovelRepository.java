package com.tramtruyen.repository;

import com.tramtruyen.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Integer> {

    List<Novel> findAllByIsDeletedFalseOrderByUpdatedAtDesc();

    List<Novel> findAllByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);

    List<Novel> findAllByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
            String title, String author);

    Optional<Novel> findByIdAndIsDeletedFalse(Integer id);
    
    Optional<Novel> findBySlugAndIsDeletedFalse(String slug);
    
    // Lấy truyện mới cập nhật
    List<Novel> findTop10ByIsDeletedFalseOrderByUpdatedAtDesc();

    // Lấy truyện Hot (Nhiều views nhất)
    List<Novel> findTop10ByIsDeletedFalseOrderByViewsDesc();

    // Lấy truyện đã hoàn thành
    List<Novel> findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);

    // Lấy truyện cùng tác giả (loại trừ truyện hiện tại)
    List<Novel> findTop5ByAuthorIgnoreCaseAndIdNotAndIsDeletedFalseOrderByViewsDesc(String author, Integer id);

    // Truyện đề cử tự động (dựa trên điểm đánh giá cao nhất)
    List<Novel> findTop10ByIsDeletedFalseOrderByAverageRatingDesc();
}
