package com.tramtruyen.repository;

import com.tramtruyen.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Integer> {
    
    // Lấy truyện mới cập nhật
    List<Novel> findTop10ByIsDeletedFalseOrderByUpdatedAtDesc();

    // Lấy truyện Hot (Nhiều views nhất)
    List<Novel> findTop10ByIsDeletedFalseOrderByViewsDesc();

    // Lấy truyện đã hoàn thành
    List<Novel> findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);
}
