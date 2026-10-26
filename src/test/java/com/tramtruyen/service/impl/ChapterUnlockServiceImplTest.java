package com.tramtruyen.service.impl;

import com.tramtruyen.dto.UnlockResultDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.Role;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.entity.enums.NovelStatus;
import com.tramtruyen.entity.enums.TransactionType;
import com.tramtruyen.exception.ChapterNotPurchasableException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.WalletService;
import com.tramtruyen.util.ChapterAccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

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

    private ChapterAccessPolicy chapterAccessPolicy;
    private ChapterUnlockServiceImpl chapterUnlockService;

    @BeforeEach
    void setUp() {
        chapterAccessPolicy = new ChapterAccessPolicy();
        chapterUnlockService = new ChapterUnlockServiceImpl(
                userRepository,
                chapterRepository,
                userChapterActivityRepository,
                walletService,
                chapterAccessPolicy
        );
    }

    @Test
    void isChapterUnlockedReturnsTrueForFreeChapter() {
        Chapter freeChapter = Chapter.builder()
                .id(1)
                .price(0)
                .status(ChapterStatus.PUBLISHED_REGULAR.name())
                .isDeleted(false)
                .build();
        when(chapterRepository.findById(1)).thenReturn(Optional.of(freeChapter));

        boolean unlocked = chapterUnlockService.isChapterUnlocked("user@example.com", 1);

        assertThat(unlocked).isTrue();
    }

    @Test
    void isChapterUnlockedReturnsTrueForStaffBypass() {
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();
        User staff = User.builder()
                .id(1)
                .email("staff@example.com")
                .roles(Set.of(Role.builder().name("ROLE_STAFF").build()))
                .build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));

        boolean unlocked = chapterUnlockService.isChapterUnlocked("staff@example.com", 2);

        assertThat(unlocked).isTrue();
    }

    @Test
    void isChapterUnlockedReturnsTrueIfPreviouslyUnlocked() {
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();
        User user = User.builder().id(10).email("user@example.com").build();
        UserChapterActivity activity = UserChapterActivity.builder().id(1).isUnlocked(true).build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.of(activity));

        boolean unlocked = chapterUnlockService.isChapterUnlocked("user@example.com", 2);

        assertThat(unlocked).isTrue();
    }

    @Test
    void unlockChapterReturnsPrivilegedResultWithoutDeduction() {
        Novel novel = Novel.builder().id(1).title("Truyện VIP").status(NovelStatus.ONGOING.name()).isDeleted(false).build();
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .novel(novel)
                .chapterNumber(15)
                .title("Chương 15")
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();
        User admin = User.builder()
                .id(9)
                .email("admin@example.com")
                .walletBalance(100)
                .roles(Set.of(Role.builder().name("ROLE_ADMIN").build()))
                .build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmailWithLock("admin@example.com")).thenReturn(Optional.of(admin));

        UnlockResultDTO result = chapterUnlockService.unlockChapter("admin@example.com", 2, false);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPricePaid()).isEqualTo(0);
        verify(walletService, never()).deductCoins(any(), anyInt(), any(), any(), any());
    }

    @Test
    void unlockChapterReturnsAlreadyUnlockedIfActivityIsUnlocked() {
        Novel novel = Novel.builder().id(1).title("Truyện VIP").status(NovelStatus.ONGOING.name()).isDeleted(false).build();
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .novel(novel)
                .chapterNumber(15)
                .title("Chương 15")
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();
        User user = User.builder().id(10).email("user@example.com").walletBalance(200).build();
        UserChapterActivity activity = UserChapterActivity.builder().id(1).isUnlocked(true).build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmailWithLock("user@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.of(activity));

        UnlockResultDTO result = chapterUnlockService.unlockChapter("user@example.com", 2, false);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPricePaid()).isEqualTo(0);
        assertThat(result.getMessage()).contains("đã mở khóa");
        verify(walletService, never()).deductCoins(any(), anyInt(), any(), any(), any());
    }

    @Test
    void unlockChapterDeductsCoinsAndSavesActivity() {
        Novel novel = Novel.builder().id(1).title("Truyện Test").status(NovelStatus.ONGOING.name()).isDeleted(false).build();
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .novel(novel)
                .chapterNumber(5)
                .title("Chương 5")
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();
        User user = User.builder().id(10).email("user@example.com").walletBalance(200).build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));
        when(userRepository.findByEmailWithLock("user@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapter(user, vipChapter)).thenReturn(Optional.empty());
        when(userChapterActivityRepository.save(any(UserChapterActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UnlockResultDTO result = chapterUnlockService.unlockChapter("user@example.com", 2, true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPricePaid()).isEqualTo(50);
        assertThat(result.getAutoUnlockNext()).isTrue();
        verify(walletService).deductCoins(eq(user), eq(50), eq(TransactionType.UNLOCK_CHAPTER.name()), eq(2), anyString());
        verify(userChapterActivityRepository).save(any(UserChapterActivity.class));
    }

    @Test
    void unlockChapterThrowsWhenNovelIsArchived() {
        Novel novel = Novel.builder().id(1).title("Truyện Ẩn").status(NovelStatus.ARCHIVED.name()).isDeleted(false).build();
        Chapter vipChapter = Chapter.builder()
                .id(2)
                .novel(novel)
                .chapterNumber(5)
                .title("Chương 5")
                .price(50)
                .status(ChapterStatus.PUBLISHED_VIP.name())
                .isDeleted(false)
                .build();

        when(chapterRepository.findById(2)).thenReturn(Optional.of(vipChapter));

        assertThatThrownBy(() -> chapterUnlockService.unlockChapter("user@example.com", 2, false))
                .isInstanceOf(ChapterNotPurchasableException.class)
                .hasMessageContaining("tạm ẩn");
    }

    @Test
    void unlockChapterThrowsWhenChapterNotFound() {
        when(chapterRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chapterUnlockService.unlockChapter("user@example.com", 999, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
