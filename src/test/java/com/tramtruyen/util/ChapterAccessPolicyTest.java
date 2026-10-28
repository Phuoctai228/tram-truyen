package com.tramtruyen.util;

import com.tramtruyen.dto.ChapterAccessStatus;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.enums.ChapterStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterAccessPolicyTest {

    private ChapterAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new ChapterAccessPolicy();
    }

    @Test
    void isPaidReturnsTrueOnlyForPublishedVipWithPriceGreaterThanZero() {
        Chapter regular = Chapter.builder().status(ChapterStatus.PUBLISHED_REGULAR.name()).price(0).build();
        Chapter draft = Chapter.builder().status(ChapterStatus.DRAFT.name()).price(50).build();
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();

        assertThat(policy.isPaid(regular)).isFalse();
        assertThat(policy.isPaid(draft)).isFalse();
        assertThat(policy.isPaid(vip)).isTrue();
    }

    @Test
    void isPrivilegedReturnsTrueForAdminAndStaff() {
        assertThat(policy.isPrivileged(Set.of("ROLE_ADMIN"))).isTrue();
        assertThat(policy.isPrivileged(Set.of("ROLE_STAFF"))).isTrue();
        assertThat(policy.isPrivileged(Set.of("ROLE_MEMBER"))).isFalse();
        assertThat(policy.isPrivileged(null)).isFalse();
    }

    @Test
    void resolveReturnsFreeForRegularChapter() {
        Chapter regular = Chapter.builder().status(ChapterStatus.PUBLISHED_REGULAR.name()).price(0).build();
        ChapterAccessStatus status = policy.resolve(regular, false, false, false, 0);
        assertThat(status).isEqualTo(ChapterAccessStatus.FREE);
    }

    @Test
    void resolveReturnsLoginRequiredWhenNotLoggedIn() {
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();
        ChapterAccessStatus status = policy.resolve(vip, false, false, false, 0);
        assertThat(status).isEqualTo(ChapterAccessStatus.LOGIN_REQUIRED);
    }

    @Test
    void resolveReturnsStaffBypassForPrivilegedRoles() {
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();
        ChapterAccessStatus status = policy.resolve(vip, true, true, false, 0);
        assertThat(status).isEqualTo(ChapterAccessStatus.STAFF_BYPASS);
    }

    @Test
    void resolveReturnsUnlockedWhenAlreadyUnlocked() {
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();
        ChapterAccessStatus status = policy.resolve(vip, true, false, true, 0);
        assertThat(status).isEqualTo(ChapterAccessStatus.UNLOCKED);
    }

    @Test
    void resolveReturnsPurchasableWhenBalanceSufficient() {
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();
        ChapterAccessStatus status = policy.resolve(vip, true, false, false, 100);
        assertThat(status).isEqualTo(ChapterAccessStatus.PURCHASABLE);
    }

    @Test
    void resolveReturnsInsufficientBalanceWhenBalanceLow() {
        Chapter vip = Chapter.builder().status(ChapterStatus.PUBLISHED_VIP.name()).price(50).build();
        ChapterAccessStatus status = policy.resolve(vip, true, false, false, 20);
        assertThat(status).isEqualTo(ChapterAccessStatus.INSUFFICIENT_BALANCE);
    }
}
