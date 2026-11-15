package com.tramtruyen.repository;

import com.tramtruyen.entity.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy xuất dữ liệu gói nạp Coin.
 */
@Repository
public interface CoinPackageRepository extends JpaRepository<CoinPackage, Integer> {

    List<CoinPackage> findByStatusOrderByPriceAsc(String status);

    List<CoinPackage> findAllByOrderByPriceAsc();

    long countByStatus(String status);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
