package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ReadingHistoryItemDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingHistoryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChapterActivityRepository userChapterActivityRepository;

    private ReadingHistoryServiceImpl readingHistoryService;

    @BeforeEach
    void setUp() {
        readingHistoryService = new ReadingHistoryServiceImpl(userRepository, userChapterActivityRepository);
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
                .isRead(true)
                .readAt(LocalDateTime.now().minusMinutes(15))
                .build();

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findAllByUserAndIsReadTrueOrderByReadAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));

        Page<ReadingHistoryItemDTO> historyPage = readingHistoryService.getReadingHistory("reader@example.com", 0, 10);

        assertThat(historyPage.getContent()).hasSize(1);
        ReadingHistoryItemDTO item = historyPage.getContent().get(0);
        assertThat(item.getNovelTitle()).isEqualTo("Đại Phụng Đả Canh Nhân");
        assertThat(item.getChapterNumber()).isEqualTo(3);
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
        when(userChapterActivityRepository.findAllByUserAndIsReadTrueOrderByReadAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(unlockedAct, freeAct)));

        readingHistoryService.clearHistory("reader@example.com");

        assertThat(unlockedAct.getIsRead()).isFalse();
        verify(userChapterActivityRepository).save(unlockedAct);
        verify(userChapterActivityRepository).delete(freeAct);
    }
}
