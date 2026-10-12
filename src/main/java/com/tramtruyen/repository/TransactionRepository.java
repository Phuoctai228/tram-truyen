package com.tramtruyen.repository;

import com.tramtruyen.entity.Transaction;
import com.tramtruyen.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistence access for wallet ledger transactions.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findAllByUserOrderByCreatedAtDesc(User user);

    Page<Transaction> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Transaction> findAllByUserAndTypeOrderByCreatedAtDesc(User user, String type, Pageable pageable);
}
