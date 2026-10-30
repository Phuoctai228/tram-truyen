package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ReadingHistoryItemDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingHistoryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChapterActivityRepository userChapterActivityRepository;

    @Mock
    private ChapterRepository chapterRepository;

    private ReadingHistoryServiceImpl readingHistoryService;

    @BeforeEach
    void setUp() {
        readingHistoryService = new ReadingHistoryServiceImpl(
                userRepository, userChapterActivityRepository, chapterRepository);
    }

    @Test
    void getReadingHistoryReturnsFormattedHistoryItems() {
        User user = User.builder().id(1).email("reader@example.com").build();
        Novel novel = Novel.builder().id(10).title("Đại Phụng Đả Canh Nhân").slug("dai-phung-da-canh-nhan").coverUrl("/images/novel.jpg").build();
        Chapter chapter = Chapter.builder().id(20).chapterNumber(3).title("Chương 3").novel(novel).build();
        UserChapterActivity activity = UserChapterActivity.builder()
                .id(100)
                .user(user)
                .chapter(chapter)
                .isRead(false)
                .readAt(LocalDateTime.now().minusMinutes(15))
                .build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findAllByUserAndReadAtIsNotNullOrderByReadAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));

        Page<ReadingHistoryItemDTO> historyPage = readingHistoryService.getReadingHistory("reader@example.com", 0, 10);

        assertThat(historyPage.getContent()).hasSize(1);
        ReadingHistoryItemDTO item = historyPage.getContent().get(0);
        assertThat(item.getNovelTitle()).isEqualTo("Đại Phụng Đả Canh Nhân");
        assertThat(item.getChapterNumber()).isEqualTo(3);
        assertThat(item.getReadUrl()).isEqualTo("/truyen/dai-phung-da-canh-nhan/chuong-3");
    }

    @Test
    void recordChapterAccessCreatesActivityWithoutMarkingCompleted() {
        User user = User.builder().id(1).email("reader@example.com").build();
        Chapter chapter = Chapter.builder().id(20).chapterNumber(3).build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(20)).thenReturn(Optional.of(chapter));
        when(userChapterActivityRepository.findByUserAndChapter(user, chapter)).thenReturn(Optional.empty());

        readingHistoryService.recordChapterAccess("reader@example.com", 20);

        ArgumentCaptor<UserChapterActivity> captor = ArgumentCaptor.forClass(UserChapterActivity.class);
        verify(userChapterActivityRepository).save(captor.capture());
        UserChapterActivity saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getChapter()).isEqualTo(chapter);
        assertThat(saved.getReadAt()).isNotNull();
        assertThat(saved.getIsRead()).isFalse();
    }

    @Test
    void recordChapterAccessUpdatesReadAtAndKeepsCompletionAndUnlockFlags() {
        User user = User.builder().id(1).email("reader@example.com").build();
        Chapter chapter = Chapter.builder().id(20).chapterNumber(3).build();
        LocalDateTime oldReadAt = LocalDateTime.now().minusDays(2);
        UserChapterActivity existing = UserChapterActivity.builder()
                .id(100).user(user).chapter(chapter)
                .isRead(true).isUnlocked(true).readAt(oldReadAt)
                .build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(20)).thenReturn(Optional.of(chapter));
        when(userChapterActivityRepository.findByUserAndChapter(user, chapter)).thenReturn(Optional.of(existing));

        readingHistoryService.recordChapterAccess("reader@example.com", 20);

        assertThat(existing.getReadAt()).isAfter(oldReadAt);
        assertThat(existing.getIsRead()).isTrue();
        assertThat(existing.getIsUnlocked()).isTrue();
        verify(userChapterActivityRepository).save(existing);
    }

    @Test
    void recordChapterAccessThrowsWhenChapterNotFound() {
        User user = User.builder().id(1).email("reader@example.com").build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(chapterRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingHistoryService.recordChapterAccess("reader@example.com", 99))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userChapterActivityRepository, never()).save(any());
    }

    @Test
    void removeHistoryItemResetsIsReadFlagIfUnlocked() {
        User user = User.builder().id(1).email("reader@example.com").build();
        UserChapterActivity activity = UserChapterActivity.builder().id(100).user(user).isRead(true).isUnlocked(true).build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapterId(user, 20)).thenReturn(Optional.of(activity));

        readingHistoryService.removeHistoryItem("reader@example.com", 20);

        assertThat(activity.getIsRead()).isFalse();
        assertThat(activity.getReadAt()).isNull();
        verify(userChapterActivityRepository).save(activity);
    }

    @Test
    void clearHistoryClearsReadFlagsForUnlockedAndDeletesFree() {
        User user = User.builder().id(1).email("reader@example.com").build();
        UserChapterActivity unlockedAct = UserChapterActivity.builder().id(1).user(user).isRead(true).isUnlocked(true).build();
        UserChapterActivity freeAct = UserChapterActivity.builder().id(2).user(user).isRead(true).isUnlocked(false).build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findAllByUserAndReadAtIsNotNullOrderByReadAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(unlockedAct, freeAct)));

        readingHistoryService.clearHistory("reader@example.com");

        assertThat(unlockedAct.getIsRead()).isFalse();
        verify(userChapterActivityRepository).save(unlockedAct);
        verify(userChapterActivityRepository).delete(freeAct);
    }
}
