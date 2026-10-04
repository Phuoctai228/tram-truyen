package com.tramtruyen.repository;

import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Integer> {
    
    List<Bookshelf> findAllByUserOrderByAddedAtDesc(User user);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Bookshelf b JOIN FETCH b.novel n WHERE b.user = :user AND n.isDeleted = false ORDER BY b.addedAt DESC")
    List<Bookshelf> findAllByUserWithNovel(@org.springframework.data.repository.query.Param("user") User user);
    
    boolean existsByUserAndNovel(User user, Novel novel);

    Optional<Bookshelf> findByUserAndNovel(User user, Novel novel);
    
    void deleteByUserAndNovel(User user, Novel novel);

    long countByNovelId(Integer novelId);
}
