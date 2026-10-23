package com.tramtruyen.repository;

import com.tramtruyen.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {


    List<Comment> findByNovelIdAndStatusOrderByCreatedAtDesc(Integer novelId, String status);

    List<Comment> findByNovelIdAndParentIsNullAndStatusOrderByCreatedAtDesc(Integer novelId, String status);

    long countByNovelIdAndStatus(Integer novelId, String status);

    @Query("SELECT c FROM Comment c WHERE c.parent.id IN :parentIds")
    List<Comment> findByParentIdIn(@Param("parentIds") Collection<Integer> parentIds);

    @Modifying
    @Query("UPDATE Comment c SET c.status = :status, c.updatedAt = :updatedAt WHERE c.id IN :ids")
    int updateStatusForIds(@Param("ids") Collection<Integer> ids, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);
}
