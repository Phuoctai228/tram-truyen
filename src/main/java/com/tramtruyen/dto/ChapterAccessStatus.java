package com.tramtruyen.dto;

/**
 * Access state of a chapter for the current visitor (UC-26). This is a computed, non-persisted state.
 *
 * <pre>
 * FREE                 chapter is not paid
 * STAFF_BYPASS         Staff/Admin reads paid chapters without being charged
 * UNLOCKED             member already paid for the chapter
 * LOGIN_REQUIRED       paid chapter, visitor is anonymous
 * INSUFFICIENT_BALANCE paid chapter, member cannot afford the price
 * PURCHASABLE          paid chapter, member can afford the price and may unlock
 * </pre>
 */
public enum ChapterAccessStatus {
    FREE,
    STAFF_BYPASS,
    UNLOCKED,
    LOGIN_REQUIRED,
    INSUFFICIENT_BALANCE,
    PURCHASABLE;

    /**
     * Whether the full chapter content may be shown.
     *
     * @return true for FREE, STAFF_BYPASS and UNLOCKED
     */
    public boolean isReadable() {
        return this == FREE || this == STAFF_BYPASS || this == UNLOCKED;
    }
}
