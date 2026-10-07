package com.tramtruyen.repository;

import com.tramtruyen.entity.NovelRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data repository for novel ratings (M4-F10).
 */
@Repository
public interface NovelRatingRepository extends JpaRepository<NovelRating, Integer> {

    Optional<NovelRating> findByNovelIdAndUserId(Integer novelId, Integer userId);

    long countByNovelId(Integer novelId);

    @Query("SELECT AVG(r.rating) FROM NovelRating r WHERE r.novel.id = :novelId")
    Double calculateAverageRatingByNovelId(@Param("novelId") Integer novelId);

    java.util.List<NovelRating> findByNovelIdOrderByUpdatedAtDesc(Integer novelId);
}
