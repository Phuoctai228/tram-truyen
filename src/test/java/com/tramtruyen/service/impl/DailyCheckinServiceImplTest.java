package com.tramtruyen.service.impl;

import com.tramtruyen.dto.DailyCheckinResultDTO;
import com.tramtruyen.dto.DailyCheckinStatusDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserDailyCheckin;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.repository.UserDailyCheckinRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyCheckinServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDailyCheckinRepository userDailyCheckinRepository;

    @Mock
    private WalletService walletService;

    private DailyCheckinServiceImpl dailyCheckinService;

    @BeforeEach
    void setUp() {
        dailyCheckinService = new DailyCheckinServiceImpl(
                userRepository,
                userDailyCheckinRepository,
                walletService
        );
    }

    @Test
    void getCheckinStatusCalculatesCorrectNextDayReward() {
        User user = User.builder().id(1).email("member@example.com").walletBalance(50).build();
        UserDailyCheckin yesterdayCheckin = UserDailyCheckin.builder()
                .id(10)
                .user(user)
                .checkinDate(LocalDate.now().minusDays(1))
                .streakCount(2)
                .rewardCoin(5)
                .build();

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(userDailyCheckinRepository.findByUserAndCheckinDate(user, LocalDate.now())).thenReturn(Optional.empty());
        when(userDailyCheckinRepository.findTopByUserOrderByCheckinDateDesc(user)).thenReturn(Optional.of(yesterdayCheckin));
        when(userDailyCheckinRepository.countByUser(user)).thenReturn(2L);

        DailyCheckinStatusDTO status = dailyCheckinService.getCheckinStatus("member@example.com");

        assertThat(status.getIsCheckedInToday()).isFalse();
        assertThat(status.getStreakCount()).isEqualTo(2);
        assertThat(status.getCurrentStreakDay()).isEqualTo(3);
        assertThat(status.getTodayReward()).isEqualTo(10); // Day 3 reward is 10
        assertThat(status.getRoadmap()).hasSize(7);
    }

    @Test
    void performCheckinAwardsCoinsAndSavesRecord() {
        User user = User.builder().id(1).email("member@example.com").build();
        UserDailyCheckin yesterdayCheckin = UserDailyCheckin.builder()
                .id(10)
                .user(user)
                .checkinDate(LocalDate.now().minusDays(1))
                .streakCount(6)
                .rewardCoin(15)
                .build();

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(userDailyCheckinRepository.existsByUserAndCheckinDate(user, LocalDate.now())).thenReturn(false);
        when(userDailyCheckinRepository.findTopByUserOrderByCheckinDateDesc(user)).thenReturn(Optional.of(yesterdayCheckin));
        when(userDailyCheckinRepository.save(any(UserDailyCheckin.class))).thenAnswer(invocation -> {
            UserDailyCheckin c = invocation.getArgument(0);
            c.setId(99);
            return c;
        });
        when(walletService.getBalance("member@example.com")).thenReturn(200);

        DailyCheckinResultDTO result = dailyCheckinService.performCheckin("member@example.com");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRewardCoin()).isEqualTo(30); // Day 7 reward is 30
        assertThat(result.getStreakCount()).isEqualTo(7);
        assertThat(result.getNewBalance()).isEqualTo(200);

        ArgumentCaptor<UserDailyCheckin> checkinCaptor = ArgumentCaptor.forClass(UserDailyCheckin.class);
        verify(userDailyCheckinRepository).save(checkinCaptor.capture());
        assertThat(checkinCaptor.getValue().getStreakCount()).isEqualTo(7);
        assertThat(checkinCaptor.getValue().getRewardCoin()).isEqualTo(30);
        verify(walletService).addCoins(eq(user), eq(30), eq("GIFT_COIN"), eq(99), anyString());
    }

    @Test
    void performCheckinRejectsDuplicateCheckinSameDay() {
        User user = User.builder().id(1).email("member@example.com").build();

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(userDailyCheckinRepository.existsByUserAndCheckinDate(user, LocalDate.now())).thenReturn(true);

        assertThatThrownBy(() -> dailyCheckinService.performCheckin("member@example.com"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Bạn đã điểm danh hôm nay rồi");

        verify(userDailyCheckinRepository, never()).save(any());
        verify(walletService, never()).addCoins(any(), anyInt(), any(), any(), any());
    }
}
