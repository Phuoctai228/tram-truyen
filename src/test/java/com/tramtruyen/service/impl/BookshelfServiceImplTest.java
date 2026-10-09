package com.tramtruyen.service.impl;

import com.tramtruyen.dto.BookshelfItemDTO;
import com.tramtruyen.dto.BookshelfPageDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.entity.Category;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.CategoryRepository;
import com.tramtruyen.repository.ChapterRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceImplTest {

    @Mock
    private BookshelfRepository bookshelfRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NovelRepository novelRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserChapterActivityRepository userChapterActivityRepository;

    private BookshelfServiceImpl bookshelfService;

    private User sampleUser;
    private Novel sampleNovel;

    @BeforeEach
    void setUp() {
        bookshelfService = new BookshelfServiceImpl(
                bookshelfRepository,
                userRepository,
                novelRepository,
                chapterRepository,
                categoryRepository,
                userChapterActivityRepository
        );

        sampleUser = User.builder()
                .id(1)
                .email("reader@test.com")
                .fullName("Độc giả Mẫu")
                .walletBalance(500)
                .build();

        Category fantasy = Category.builder().id(1).name("Tiên Hiệp").slug("tien-hiep").build();

        sampleNovel = Novel.builder()
                .id(10)
                .title("Quỷ Bí Chi Chủ")
                .author("Mực Thích Lặn Nước")
                .coverUrl("https://example.com/cover.jpg")
                .status("ONGOING")
                .categories(Set.of(fantasy))
                .build();
    }

    @Test
    void getBookshelfPageReturnsEmptyWhenUserHasNoSavedNovels() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        when(bookshelfRepository.findAllByUserWithNovel(sampleUser)).thenReturn(Collections.emptyList());
        when(categoryRepository.findAll()).thenReturn(List.of(Category.builder().name("Tiên Hiệp").build()));

        BookshelfPageDTO page = bookshelfService.getBookshelfPage("reader@test.com", "ALL", null, "recent", 1, 8);

        assertThat(page.getTotalCount()).isZero();
        assertThat(page.getItems()).isEmpty();
        assertThat(page.getPopularCategories()).hasSize(1);
    }

    @Test
    void getBookshelfPageCalculatesProgressCorrectly() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));

        Bookshelf entry = Bookshelf.builder()
                .id(100)
                .user(sampleUser)
                .novel(sampleNovel)
                .addedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(bookshelfRepository.findAllByUserWithNovel(sampleUser)).thenReturn(List.of(entry));
        when(chapterRepository.countByNovelIdAndIsDeletedFalse(10)).thenReturn(100L);
        when(chapterRepository.existsByNovelIdAndPriceGreaterThanAndIsDeletedFalse(10, 0)).thenReturn(true);
        when(userChapterActivityRepository.countByUserAndChapter_NovelAndIsReadTrue(sampleUser, sampleNovel)).thenReturn(37L);

        Chapter chap37 = Chapter.builder().id(37).chapterNumber(37).title("Chương 37").build();
        UserChapterActivity activity = UserChapterActivity.builder()
                .user(sampleUser)
                .chapter(chap37)
                .isRead(true)
                .readAt(LocalDateTime.now().minusMinutes(15))
                .build();

        when(userChapterActivityRepository.findFirstByUserAndChapter_NovelAndIsReadTrueOrderByReadAtDescChapter_ChapterNumberDesc(sampleUser, sampleNovel))
                .thenReturn(Optional.of(activity));

        BookshelfPageDTO page = bookshelfService.getBookshelfPage("reader@test.com", "ALL", null, "recent", 1, 8);

        assertThat(page.getTotalCount()).isEqualTo(1);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getProgressPercentage()).isEqualTo(37);
        assertThat(page.getItems().get(0).getCurrentChapterNumber()).isEqualTo(37);
        assertThat(page.getItems().get(0).isCompleted()).isFalse();
        assertThat(page.getItems().get(0).isHasVipChapters()).isTrue();
        assertThat(page.getItems().get(0).getReadUrl()).isEqualTo("/truyen/quy-bi-chi-chu/chuong-38");
    }

    @Test
    void addNovelToBookshelfThrowsDuplicateWhenAlreadySaved() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        when(novelRepository.findByIdAndIsDeletedFalse(10)).thenReturn(Optional.of(sampleNovel));
        when(bookshelfRepository.existsByUserAndNovel(sampleUser, sampleNovel)).thenReturn(true);

        assertThatThrownBy(() -> bookshelfService.addNovelToBookshelf("reader@test.com", 10))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addNovelToBookshelfSavesSuccessfully() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        when(novelRepository.findByIdAndIsDeletedFalse(10)).thenReturn(Optional.of(sampleNovel));
        when(bookshelfRepository.existsByUserAndNovel(sampleUser, sampleNovel)).thenReturn(false);

        bookshelfService.addNovelToBookshelf("reader@test.com", 10);

        verify(bookshelfRepository).save(any(Bookshelf.class));
    }

    @Test
    void removeNovelFromBookshelfDeletesEntry() {
        Bookshelf entry = Bookshelf.builder().id(5).user(sampleUser).novel(sampleNovel).build();
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        when(novelRepository.findByIdAndIsDeletedFalse(10)).thenReturn(Optional.of(sampleNovel));
        when(bookshelfRepository.findByUserAndNovel(sampleUser, sampleNovel)).thenReturn(Optional.of(entry));

        bookshelfService.removeNovelFromBookshelf("reader@test.com", 10);

        verify(bookshelfRepository).delete(entry);
    }

    @Test
    void getUserProfileReturnsAccurateInformationWithoutFakeTags() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));

        UserProfileDTO profile = bookshelfService.getUserProfile("reader@test.com");

        assertThat(profile.getFullName()).isEqualTo("Độc giả Mẫu");
        assertThat(profile.getEmail()).isEqualTo("reader@test.com");
        assertThat(profile.getWalletBalance()).isEqualTo(500);
    }

    @Test
    void getBookshelfPageMarksOngoingNovelAsCaughtUpWhenAllChaptersRead() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        sampleNovel.setStatus("ONGOING");

        Bookshelf entry = Bookshelf.builder()
                .id(101)
                .user(sampleUser)
                .novel(sampleNovel)
                .addedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(bookshelfRepository.findAllByUserWithNovel(sampleUser)).thenReturn(List.of(entry));
        when(chapterRepository.countByNovelIdAndIsDeletedFalse(10)).thenReturn(5L);
        when(chapterRepository.existsByNovelIdAndPriceGreaterThanAndIsDeletedFalse(10, 0)).thenReturn(false);
        when(userChapterActivityRepository.countByUserAndChapter_NovelAndIsReadTrue(sampleUser, sampleNovel)).thenReturn(5L);

        Chapter chap5 = Chapter.builder().id(5).chapterNumber(5).title("Chương 5").build();
        UserChapterActivity activity = UserChapterActivity.builder()
                .user(sampleUser)
                .chapter(chap5)
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();

        when(userChapterActivityRepository.findFirstByUserAndChapter_NovelAndIsReadTrueOrderByReadAtDescChapter_ChapterNumberDesc(sampleUser, sampleNovel))
                .thenReturn(Optional.of(activity));

        BookshelfPageDTO page = bookshelfService.getBookshelfPage("reader@test.com", "ALL", null, "recent", 1, 8);

        assertThat(page.getItems()).hasSize(1);
        BookshelfItemDTO item = page.getItems().get(0);
        assertThat(item.getProgressPercentage()).isEqualTo(100);
        assertThat(item.isCompleted()).isFalse();
        assertThat(item.isCaughtUp()).isTrue();
        assertThat(page.getCompletedCount()).isZero();
        assertThat(page.getReadingCount()).isEqualTo(1);
    }

    @Test
    void getBookshelfPageMarksCompletedNovelAsCompletedWhenAllChaptersRead() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        sampleNovel.setStatus("COMPLETED");

        Bookshelf entry = Bookshelf.builder()
                .id(102)
                .user(sampleUser)
                .novel(sampleNovel)
                .addedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(bookshelfRepository.findAllByUserWithNovel(sampleUser)).thenReturn(List.of(entry));
        when(chapterRepository.countByNovelIdAndIsDeletedFalse(10)).thenReturn(5L);
        when(chapterRepository.existsByNovelIdAndPriceGreaterThanAndIsDeletedFalse(10, 0)).thenReturn(false);
        when(userChapterActivityRepository.countByUserAndChapter_NovelAndIsReadTrue(sampleUser, sampleNovel)).thenReturn(5L);

        Chapter chap5 = Chapter.builder().id(5).chapterNumber(5).title("Chương 5").build();
        UserChapterActivity activity = UserChapterActivity.builder()
                .user(sampleUser)
                .chapter(chap5)
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();

        when(userChapterActivityRepository.findFirstByUserAndChapter_NovelAndIsReadTrueOrderByReadAtDescChapter_ChapterNumberDesc(sampleUser, sampleNovel))
                .thenReturn(Optional.of(activity));

        BookshelfPageDTO page = bookshelfService.getBookshelfPage("reader@test.com", "ALL", null, "recent", 1, 8);

        assertThat(page.getItems()).hasSize(1);
        BookshelfItemDTO item = page.getItems().get(0);
        assertThat(item.getProgressPercentage()).isEqualTo(100);
        assertThat(item.isCompleted()).isTrue();
        assertThat(item.isCaughtUp()).isFalse();
        assertThat(page.getCompletedCount()).isEqualTo(1);
        assertThat(page.getReadingCount()).isZero();
    }

    @Test
    void isChapterReadReturnsTrueWhenActivityHasIsReadTrue() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        Chapter chap = Chapter.builder().id(20).build();
        when(chapterRepository.findById(20)).thenReturn(Optional.of(chap));

        UserChapterActivity activity = UserChapterActivity.builder()
                .user(sampleUser)
                .chapter(chap)
                .isRead(true)
                .build();
        when(userChapterActivityRepository.findByUserAndChapter(sampleUser, chap)).thenReturn(Optional.of(activity));

        boolean read = bookshelfService.isChapterRead("reader@test.com", 20);

        assertThat(read).isTrue();
    }

    @Test
    void isChapterReadReturnsFalseWhenNoActivityOrNotRead() {
        when(userRepository.findByEmail("reader@test.com")).thenReturn(Optional.of(sampleUser));
        Chapter chap = Chapter.builder().id(21).build();
        when(chapterRepository.findById(21)).thenReturn(Optional.of(chap));
        when(userChapterActivityRepository.findByUserAndChapter(sampleUser, chap)).thenReturn(Optional.empty());

        boolean read = bookshelfService.isChapterRead("reader@test.com", 21);

        assertThat(read).isFalse();
    }
}
