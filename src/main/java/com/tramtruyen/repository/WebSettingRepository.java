package com.tramtruyen.repository;

import com.tramtruyen.entity.WebSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebSettingRepository extends JpaRepository<WebSetting, Integer> {

    Optional<WebSetting> findFirstByIsActiveTrueOrderByIdAsc();

    Optional<WebSetting> findByFolderPath(String folderPath);

    boolean existsByFolderPath(String folderPath);

    List<WebSetting> findAllByOrderByIdAsc();

    @Modifying
    @Query("UPDATE WebSetting w SET w.isActive = false WHERE w.isActive = true")
    void deactivateAllActiveThemes();
}
