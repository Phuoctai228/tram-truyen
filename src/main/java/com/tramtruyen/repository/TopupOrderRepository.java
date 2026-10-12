package com.tramtruyen.repository;

import com.tramtruyen.entity.TopupOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy xuất dữ liệu đơn nạp tiền.
 */
@Repository
public interface TopupOrderRepository extends JpaRepository<TopupOrder, Integer> {

    Optional<TopupOrder> findByOrderCode(String orderCode);

    List<TopupOrder> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<TopupOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, String status);
}
