package com.tramtruyen.repository;

import com.tramtruyen.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT n FROM Notification n WHERE (n.userId = :userId OR n.userId IS NULL) " +
           "AND (n.title IS NULL OR LOWER(n.title) NOT LIKE LOWER(CONCAT(:reportPrefix, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findGeneralNotifications(@Param("userId") Integer userId, @Param("reportPrefix") String reportPrefix, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE (n.userId = :userId OR n.userId IS NULL) " +
           "AND LOWER(n.title) LIKE LOWER(CONCAT(:reportPrefix, '%')) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findReportNotifications(@Param("userId") Integer userId, @Param("reportPrefix") String reportPrefix, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE (n.userId = :userId OR n.userId IS NULL) ORDER BY n.createdAt DESC")
    Page<Notification> findUserAndSystemNotifications(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE (n.userId = :userId OR n.userId IS NULL) AND LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY n.createdAt DESC")
    Page<Notification> findUserAndSystemNotificationsByKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL)")
    long countUnreadByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL) " +
           "AND (n.title IS NULL OR LOWER(n.title) NOT LIKE LOWER(CONCAT(:reportPrefix, '%')))")
    long countUnreadGeneralByUserId(@Param("userId") Integer userId, @Param("reportPrefix") String reportPrefix);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL) " +
           "AND LOWER(n.title) LIKE LOWER(CONCAT(:reportPrefix, '%'))")
    long countUnreadReportByUserId(@Param("userId") Integer userId, @Param("reportPrefix") String reportPrefix);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND (n.isRead = false OR n.isRead IS NULL)")
    int markAllAsReadByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "INSERT INTO notifications (user_id, title, content, is_read, created_at) " +
                   "SELECT b.user_id, :title, :content, false, NOW() " +
                   "FROM bookshelves b " +
                   "JOIN novels n ON n.id = b.novel_id " +
                   "JOIN chapters c ON c.id = :chapterId AND c.novel_id = b.novel_id " +
                   "WHERE b.novel_id = :novelId " +
                   "  AND n.is_deleted = false " +
                   "  AND n.status != 'ARCHIVED' " +
                   "  AND c.is_deleted = false " +
                   "  AND c.status IN ('PUBLISHED', 'PUBLISHED_REGULAR', 'PUBLISHED_VIP') " +
                   "  AND (:uploaderId IS NULL OR b.user_id != :uploaderId) " +
                   "  AND NOT EXISTS ( " +
                   "      SELECT 1 FROM notifications notif " +
                   "      WHERE notif.user_id = b.user_id AND notif.title = :title " +
                   "  )", nativeQuery = true)
    int insertChapterNotificationsForFollowers(@Param("novelId") Integer novelId,
                                               @Param("chapterId") Integer chapterId,
                                               @Param("title") String title,
                                               @Param("content") String content,
                                               @Param("uploaderId") Integer uploaderId);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
