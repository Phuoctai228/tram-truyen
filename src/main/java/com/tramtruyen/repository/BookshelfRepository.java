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
    
    boolean existsByUserAndNovel(User user, Novel novel);
    
    Optional<Bookshelf> findByUserAndNovel(User user, Novel novel);
    
    void deleteByUserAndNovel(User user, Novel novel);
}
