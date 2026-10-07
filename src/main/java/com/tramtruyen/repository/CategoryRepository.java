package com.tramtruyen.repository;

import com.tramtruyen.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByNameAndIdNot(String name, Integer id);
    boolean existsBySlugAndIdNot(String slug, Integer id);
    List<Category> findByNameContainingIgnoreCase(String name);
    Optional<Category> findBySlug(String slug);
}
