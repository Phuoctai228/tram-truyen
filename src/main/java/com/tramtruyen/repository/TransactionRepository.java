package com.tramtruyen.repository;

import com.tramtruyen.entity.Transaction;
import com.tramtruyen.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository lưu vết sổ cái ví Coin (Transaction ledger).
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findAllByUserOrderByCreatedAtDesc(User user);

    Page<Transaction> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Transaction> findAllByUserAndTypeOrderByCreatedAtDesc(User user, String type, Pageable pageable);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Optional<Transaction> findFirstByUserIdOrderByCreatedAtDesc(Integer userId);

    long countByUserId(Integer userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:type IS NULL OR :type = '' OR t.type = :type) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR CAST(t.id AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> searchTransactions(@Param("userId") Integer userId,
                                         @Param("type") String type,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
}
