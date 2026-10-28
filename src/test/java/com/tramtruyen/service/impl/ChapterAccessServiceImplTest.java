package com.tramtruyen.service.impl;

import com.tramtruyen.dto.ChapterAccessDTO;
import com.tramtruyen.dto.ChapterAccessStatus;
import com.tramtruyen.dto.CoinPackageDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.UserChapterActivity;
import com.tramtruyen.entity.enums.ChapterStatus;
import com.tramtruyen.repository.UserChapterActivityRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.CoinPackageService;
import com.tramtruyen.util.ChapterAccessPolicy;
import com.tramtruyen.util.ChapterPreviewBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterAccessServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserChapterActivityRepository userChapterActivityRepository;

    @Mock
    private CoinPackageService coinPackageService;

    private ChapterAccessPolicy chapterAccessPolicy;
    private ChapterPreviewBuilder chapterPreviewBuilder;
    private ChapterAccessServiceImpl chapterAccessService;

    @BeforeEach
    void setUp() {
        chapterAccessPolicy = new ChapterAccessPolicy();
        chapterPreviewBuilder = new ChapterPreviewBuilder();
        chapterAccessService = new ChapterAccessServiceImpl(
                userRepository,
                userChapterActivityRepository,
                chapterAccessPolicy,
                chapterPreviewBuilder,
                coinPackageService
        );
    }

    @Test
    void checkAccessReturnsFreeForRegularChapter() {
        Novel novel = Novel.builder().id(1).title("Truyện Test").build();
        Chapter chapter = Chapter.builder().id(1).novel(novel).price(0).status(ChapterStatus.PUBLISHED_REGULAR.name()).content("Nội dung").build();

        ChapterAccessDTO dto = chapterAccessService.checkAccess(novel, chapter, null);

        assertThat(dto.getStatus()).isEqualTo(ChapterAccessStatus.FREE);
        assertThat(dto.isReadable()).isTrue();
        assertThat(dto.isPaid()).isFalse();
        assertThat(dto.getWatermarkText()).contains("Khách đọc").contains("Trạm Truyện");
        assertThat(dto.getUserFingerprint()).isEqualTo("GUEST");
    }

    @Test
    void checkAccessReturnsInsufficientBalanceAndSuggestionsWhenUserCannotAfford() {
        Novel novel = Novel.builder().id(1).title("Truyện Test").build();
        Chapter chapter = Chapter.builder().id(2).novel(novel).price(50).status(ChapterStatus.PUBLISHED_VIP.name()).content("<p>Đoạn 1</p><p>Đoạn 2</p>").build();
        User user = User.builder().id(10).email("reader@example.com").walletBalance(20).build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "reader@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
        when(userChapterActivityRepository.findByUserAndChapter(user, chapter)).thenReturn(Optional.empty());
        when(coinPackageService.suggestForShortfall(eq(30), anyInt())).thenReturn(List.of(
                CoinPackageDTO.builder().id(1).coinAmount(50).price(50000).bestFit(true).build()
        ));

        ChapterAccessDTO dto = chapterAccessService.checkAccess(novel, chapter, auth);

        assertThat(dto.getStatus()).isEqualTo(ChapterAccessStatus.INSUFFICIENT_BALANCE);
        assertThat(dto.isReadable()).isFalse();
        assertThat(dto.getShortfall()).isEqualTo(30);
        assertThat(dto.getSuggestedPackages()).hasSize(1);
        assertThat(dto.getSuggestedPackages().get(0).isBestFit()).isTrue();
        assertThat(dto.getWatermarkText()).contains("UID: 10").contains("reader@example.com").contains("Trạm Truyện");
        assertThat(dto.getUserFingerprint()).isEqualTo("UID:10");
    }

    @Test
    void checkAccessGeneratesForensicWatermarkWithFullNameForLoggedInUser() {
        Novel novel = Novel.builder().id(1).title("Truyện Test").build();
        Chapter chapter = Chapter.builder().id(3).novel(novel).price(0).status(ChapterStatus.PUBLISHED_REGULAR.name()).content("Nội dung").build();
        User user = User.builder().id(25).email("vip@example.com").fullName("Lê Văn VIP").walletBalance(100).build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "vip@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));

        when(userRepository.findByEmail("vip@example.com")).thenReturn(Optional.of(user));

        ChapterAccessDTO dto = chapterAccessService.checkAccess(novel, chapter, auth);

        assertThat(dto.getWatermarkText()).contains("UID: 25").contains("Lê Văn VIP").contains("Trạm Truyện");
        assertThat(dto.getUserFingerprint()).isEqualTo("UID:25");
    }
}
