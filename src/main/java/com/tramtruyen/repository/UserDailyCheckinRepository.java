package com.tramtruyen.repository;

import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserDailyCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Persistence access for user daily attendance check-ins.
 */
@Repository
public interface UserDailyCheckinRepository extends JpaRepository<UserDailyCheckin, Integer> {

    Optional<UserDailyCheckin> findByUserAndCheckinDate(User user, LocalDate checkinDate);

    Optional<UserDailyCheckin> findTopByUserOrderByCheckinDateDesc(User user);

    boolean existsByUserAndCheckinDate(User user, LocalDate checkinDate);

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(c.rewardCoin), 0) FROM UserDailyCheckin c WHERE c.user = :user")
    Long sumRewardCoinByUser(@Param("user") User user);
}
