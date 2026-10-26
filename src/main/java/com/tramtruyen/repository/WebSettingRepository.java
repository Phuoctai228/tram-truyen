package com.tramtruyen.repository;

import com.tramtruyen.entity.WebSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebSettingRepository extends JpaRepository<WebSetting, Integer> {

    Optional<WebSetting> findByIsActiveTrue();

    Optional<WebSetting> findByFolderPath(String folderPath);

    List<WebSetting> findAllByOrderByIdAsc();
}
