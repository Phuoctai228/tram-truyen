package com.tramtruyen.repository;

import com.tramtruyen.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Integer>, JpaSpecificationExecutor<Novel> {

    List<Novel> findAllByIsDeletedFalseOrderByUpdatedAtDesc();

    List<Novel> findAllByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);

    List<Novel> findAllByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
            String title, String author);

    Optional<Novel> findByIdAndIsDeletedFalse(Integer id);

    Optional<Novel> findByIdAndIsDeletedFalseAndStatusNot(Integer id, String status);
    
    Optional<Novel> findBySlugAndIsDeletedFalse(String slug);

    Optional<Novel> findBySlugAndIsDeletedFalseAndStatusNot(String slug, String status);
    
    // Lấy truyện mới cập nhật (2 hàng x 6 cột trên trang chủ)
    List<Novel> findTop12ByIsDeletedFalseOrderByUpdatedAtDesc();
    List<Novel> findTop10ByIsDeletedFalseOrderByUpdatedAtDesc();

    // Lấy truyện Hot (Nhiều views nhất - Top 10 Bảng xếp hạng)
    List<Novel> findTop10ByIsDeletedFalseOrderByViewsDesc();

    // Lấy truyện đã hoàn thành
    List<Novel> findTop12ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);
    List<Novel> findTop10ByIsDeletedFalseAndStatusOrderByUpdatedAtDesc(String status);

    // Lấy truyện cùng tác giả (loại trừ truyện hiện tại)
    List<Novel> findTop5ByAuthorIgnoreCaseAndIdNotAndIsDeletedFalseOrderByViewsDesc(String author, Integer id);

    // Truyện đề cử tự động (chỉ lấy truyện có ít nhất 1 chương công khai, sắp xếp theo điểm đánh giá và lượt xem)
    @Query("SELECT n FROM Novel n WHERE n.isDeleted = false AND n.status <> 'ARCHIVED' " +
           "AND EXISTS (SELECT 1 FROM Chapter c WHERE c.novel = n AND c.isDeleted = false AND c.status IN :publicStatuses) " +
           "ORDER BY n.averageRating DESC, n.views DESC")
    List<Novel> findTopRecommendedNovels(@Param("publicStatuses") Collection<String> publicStatuses, Pageable pageable);

    // Truyện đề cử tự động (2 hàng x 6 cột trên trang chủ)
    List<Novel> findTop12ByIsDeletedFalseOrderByAverageRatingDesc();
    List<Novel> findTop10ByIsDeletedFalseOrderByAverageRatingDesc();

    // Lấy truyện mới đăng gần đây (2 hàng x 6 cột trên trang chủ)
    List<Novel> findTop12ByIsDeletedFalseOrderByCreatedAtDesc();

    boolean existsByCategoriesIdAndIsDeletedFalse(Integer categoryId);

    List<Novel> findByCategoriesIdAndIsDeletedFalseOrderByUpdatedAtDesc(Integer categoryId);

    Page<Novel> findByCategoriesIdAndIsDeletedFalseOrderByUpdatedAtDesc(Integer categoryId, Pageable pageable);

    List<Novel> findByCategoriesIdAndIsDeletedFalseAndStatusNotOrderByUpdatedAtDesc(
            Integer categoryId, String status);

    Page<Novel> findByCategoriesIdAndIsDeletedFalseAndStatusNotOrderByUpdatedAtDesc(
            Integer categoryId, String status, Pageable pageable);

    List<Novel> findAllByIsDeletedFalseOrderByViewsDesc();

    List<Novel> findByCategoriesIdAndIsDeletedFalseOrderByViewsDesc(Integer categoryId);
}
