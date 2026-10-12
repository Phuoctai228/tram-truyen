package com.tramtruyen.service.impl;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChapterUnlockServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private UserChapterActivityRepository userChapterActivityRepository;

    @Mock
    private WalletService walletService;

    private ChapterUnlockServiceImpl chapterUnlockService;

    @BeforeEach
    void setUp() {
        chapterUnlockService = new ChapterUnlockServiceImpl(
                userRepository,
                chapterRepository,
                userChapterActivityRepository,
                walletService
        );
    }

    @Test
    void isChapterUnlockedReturnsTrueForFreeChapter() {
        Chapter freeChapter = Chapter.builder().id(1).price(0).build();
        when(chapterRepository.findById(1)).thenReturn(Optional.of(freeChapter));

        boolean unlocked = chapterUnlockService.isChapterUnlocked("user@example.com", 1);

        assertThat(unlocked).isTrue();
    }

    @Test
    void isChapterUnlockedReturnsTrueIfPreviouslyUnlocked() {
        Chapter vipChapter = Chapter.builder().id(2).price(50).build();
        User user = User.builder().id(10).email("user@example.com").build();
        UserChapterActivity activity = UserChapterActivity.builder().id(1).isUnlocked(true).build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.of(activity));

        boolean unlocked = chapterUnlockService.isChapterUnlocked("user@example.com", 2);

        assertThat(unlocked).isTrue();
    }

    @Test
    void unlockChapterReturnsAlreadyUnlockedIfActivityIsUnlocked() {
        Chapter vipChapter = Chapter.builder().id(2).price(50).build();
        User user = User.builder().id(10).email("user@example.com").build();
        UserChapterActivity activity = UserChapterActivity.builder().id(1).isUnlocked(true).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.of(activity));
        when(walletService.getBalance("user@example.com")).thenReturn(100);

        UnlockResultDTO result = chapterUnlockService.unlockChapter("user@example.com", 2, false);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("đã mở khóa");
        verify(walletService, never()).deductCoins(any(), anyInt(), any(), any(), any());
    }

    @Test
    void unlockChapterDeductsCoinsAndSavesActivity() {
        Novel novel = Novel.builder().id(1).title("Truyện Test").build();
        Chapter vipChapter = Chapter.builder().id(2).chapterNumber(5).title("Chương 5").price(50).novel(novel).build();
        User user = User.builder().id(10).email("user@example.com").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.empty());
        when(walletService.getBalance("user@example.com")).thenReturn(150);
        when(userChapterActivityRepository.save(any(UserChapterActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UnlockResultDTO result = chapterUnlockService.unlockChapter("user@example.com", 2, true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getRemainingBalance()).isEqualTo(150);
        assertThat(result.getAutoUnlockNext()).isTrue();
        verify(walletService).deductCoins(eq(user), eq(50), eq("UNLOCK_CHAPTER"), eq(2), anyString());
        verify(userChapterActivityRepository).save(any(UserChapterActivity.class));
    }

    @Test
    void unlockChapterThrowsWhenChapterNotFound() {
        User user = User.builder().id(10).email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterUnlockService.unlockChapter("user@example.com", 999, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
