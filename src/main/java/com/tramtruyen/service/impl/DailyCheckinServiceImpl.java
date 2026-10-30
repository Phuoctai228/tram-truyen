package com.tramtruyen.service.impl;

import com.tramtruyen.dto.DailyCheckinResultDTO;
import com.tramtruyen.dto.DailyCheckinStatusDTO;
import com.tramtruyen.dto.DailyCheckinStatusDTO.DayRoadmapItemDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserDailyCheckin;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.UserDailyCheckinRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.DailyCheckinService;
import com.tramtruyen.service.WalletService;
import com.tramtruyen.entity.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Production implementation of DailyCheckinService matching RDS 7-day reward roadmap (image38.png).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyCheckinServiceImpl implements DailyCheckinService {

    // Rewards matching RDS image38.png: +5, +5, +10, +10, +15, +15, +30 Coin
    private static final int[] REWARD_SCHEDULE = {5, 5, 10, 10, 15, 15, 30};

    private final UserRepository userRepository;
    private final UserDailyCheckinRepository userDailyCheckinRepository;
    private final WalletService walletService;

    @Override
    @Transactional(readOnly = true)
    public DailyCheckinStatusDTO getCheckinStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        LocalDate today = LocalDate.now();
        Optional<UserDailyCheckin> todayCheckinOpt = userDailyCheckinRepository.findByUserAndCheckinDate(user, today);
        boolean isCheckedInToday = todayCheckinOpt.isPresent();

        Optional<UserDailyCheckin> latestCheckinOpt = userDailyCheckinRepository.findTopByUserOrderByCheckinDateDesc(user);

        int streakCount = 0;
        int targetStreakForToday = 1;

        if (isCheckedInToday) {
            streakCount = todayCheckinOpt.get().getStreakCount();
            targetStreakForToday = streakCount;
        } else if (latestCheckinOpt.isPresent()) {
            UserDailyCheckin latest = latestCheckinOpt.get();
            if (latest.getCheckinDate().equals(today.minusDays(1))) {
                // Streak is maintained from yesterday
                streakCount = latest.getStreakCount();
                targetStreakForToday = streakCount + 1;
            } else {
                // Streak broken
                streakCount = 0;
                targetStreakForToday = 1;
            }
        }

        // Map to 1..7 cycle
        int currentCycleDay = ((targetStreakForToday - 1) % 7) + 1;
        int todayReward = REWARD_SCHEDULE[currentCycleDay - 1];

        // Format dates & weeks
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = today.get(weekFields.weekOfWeekBasedYear());
        int year = today.getYear();
        String formattedServerDate = "Ngày " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        long totalCheckinDays = userDailyCheckinRepository.countByUser(user);
        int walletBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        Long totalRewardCoins = userDailyCheckinRepository.sumRewardCoinByUser(user);
        int totalRewards = totalRewardCoins != null ? totalRewardCoins.intValue() : 0;

        // Build 7-day roadmap
        List<DayRoadmapItemDTO> roadmap = buildRoadmap(currentCycleDay, isCheckedInToday);

        return DailyCheckinStatusDTO.builder()
                .serverDate(formattedServerDate)
                .weekNumber(weekNumber)
                .year(year)
                .totalCheckinDays(totalCheckinDays)
                .streakCount(streakCount)
                .currentStreakDay(currentCycleDay)
                .todayReward(todayReward)
                .isCheckedInToday(isCheckedInToday)
                .walletBalance(walletBalance)
                .totalRewardCoins(totalRewards)
                .roadmap(roadmap)
                .build();
    }

    @Override
    @Transactional
    public DailyCheckinResultDTO performCheckin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vui lòng đăng nhập để điểm danh"));

        LocalDate today = LocalDate.now();

        if (userDailyCheckinRepository.existsByUserAndCheckinDate(user, today)) {
            throw new DuplicateResourceException("Bạn đã điểm danh hôm nay rồi! Hãy quay lại vào ngày mai.");
        }

        // Determine new streak count
        Optional<UserDailyCheckin> latestCheckinOpt = userDailyCheckinRepository.findTopByUserOrderByCheckinDateDesc(user);
        int newStreak = 1;
        if (latestCheckinOpt.isPresent()) {
            UserDailyCheckin latest = latestCheckinOpt.get();
            if (latest.getCheckinDate().equals(today.minusDays(1))) {
                newStreak = latest.getStreakCount() + 1;
            }
        }

        int cycleDay = ((newStreak - 1) % 7) + 1;
        int rewardCoins = REWARD_SCHEDULE[cycleDay - 1];

        // Persist checkin record
        UserDailyCheckin checkin = UserDailyCheckin.builder()
                .user(user)
                .checkinDate(today)
                .rewardCoin(rewardCoins)
                .streakCount(newStreak)
                .build();
        userDailyCheckinRepository.save(checkin);

        // Credit coins to wallet ledger
        String description = "Điểm danh hàng ngày (Chuỗi " + newStreak + " ngày, mốc +" + rewardCoins + " Coin)";
        walletService.addCoins(user, rewardCoins, TransactionType.GIFT_COIN.name(), checkin.getId(), description);

        int newBalance = walletService.getBalance(email);

        log.info("User {} checked in successfully today. Streak: {}, Reward: {} Coin", email, newStreak, rewardCoins);

        return DailyCheckinResultDTO.builder()
                .success(true)
                .rewardCoin(rewardCoins)
                .streakCount(newStreak)
                .newBalance(newBalance)
                .message("Điểm danh thành công! Bạn nhận được +" + rewardCoins + " Coin.")
                .build();
    }

    private List<DayRoadmapItemDTO> buildRoadmap(int currentCycleDay, boolean isCheckedInToday) {
        List<DayRoadmapItemDTO> items = new ArrayList<>(7);
        for (int i = 1; i <= 7; i++) {
            int reward = REWARD_SCHEDULE[i - 1];
            boolean isSpecial = (i == 7);
            String status;
            String label;

            if (i < currentCycleDay) {
                status = "CLAIMED";
                label = (i == currentCycleDay - 1) ? "Hôm qua" : "Đã nhận";
            } else if (i == currentCycleDay) {
                if (isCheckedInToday) {
                    status = "TODAY_CLAIMED";
                    label = "Đã nhận";
                } else {
                    status = "TODAY_PENDING";
                    label = "Chờ nhận";
                }
            } else {
                status = "LOCKED";
                label = (i == currentCycleDay + 1) ? "Ngày mai" : (isSpecial ? "Thưởng mốc: +" + reward + " Coin" : "Chưa mở");
            }

            items.add(DayRoadmapItemDTO.builder()
                    .dayNumber(i)
                    .rewardCoin(reward)
                    .status(status)
                    .label(label)
                    .isSpecial(isSpecial)
                    .build());
        }
        return items;
    }
}
