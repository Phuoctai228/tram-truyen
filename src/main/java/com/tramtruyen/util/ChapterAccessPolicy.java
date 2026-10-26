package com.tramtruyen.util;

import com.tramtruyen.dto.ChapterAccessStatus;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.enums.ChapterStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Pure business rules deciding whether a chapter is paid and what access state a visitor has (UC-26).
 * Stateless and free of I/O so it can be unit-tested in isolation.
 */
@Component
public class ChapterAccessPolicy {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_STAFF = "ROLE_STAFF";

    /**
     * A chapter is paid only when it is published as VIP and has a positive Coin price.
     *
     * @param chapter chapter to evaluate
     * @return true when readers must pay to read it
     */
    public boolean isPaid(Chapter chapter) {
        return chapter != null
                && ChapterStatus.PUBLISHED_VIP.name().equals(chapter.getStatus())
                && chapter.getPrice() != null
                && chapter.getPrice() > 0;
    }

    /**
     * Whether any of the given role names grants free reading (Staff or Admin).
     *
     * @param roleNames role names of the user
     * @return true for ROLE_STAFF or ROLE_ADMIN
     */
    public boolean isPrivileged(Collection<String> roleNames) {
        return roleNames != null && (roleNames.contains(ROLE_ADMIN) || roleNames.contains(ROLE_STAFF));
    }

    /**
     * Resolves the access state. Evaluation order is significant: free, anonymous, privileged, unlocked, balance.
     *
     * @param chapter chapter being opened
     * @param loggedIn whether the visitor is authenticated and exists
     * @param privileged whether the visitor is Staff/Admin
     * @param alreadyUnlocked whether the visitor already paid for this chapter
     * @param balance current Coin balance (ignored unless needed)
     * @return resolved access status
     */
    public ChapterAccessStatus resolve(Chapter chapter, boolean loggedIn, boolean privileged,
                                       boolean alreadyUnlocked, int balance) {
        if (!isPaid(chapter)) {
            return ChapterAccessStatus.FREE;
        }
        if (!loggedIn) {
            return ChapterAccessStatus.LOGIN_REQUIRED;
        }
        if (privileged) {
            return ChapterAccessStatus.STAFF_BYPASS;
        }
        if (alreadyUnlocked) {
            return ChapterAccessStatus.UNLOCKED;
        }
        return balance >= chapter.getPrice()
                ? ChapterAccessStatus.PURCHASABLE
                : ChapterAccessStatus.INSUFFICIENT_BALANCE;
    }
}
