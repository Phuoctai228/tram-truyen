package com.tramtruyen.repository;

import com.tramtruyen.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository lưu trữ nhật ký Webhook / IPN.
 */
@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Integer> {
}
