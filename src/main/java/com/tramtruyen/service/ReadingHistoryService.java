package com.tramtruyen.service;

import com.tramtruyen.dto.ReadingHistoryItemDTO;
import org.springframework.data.domain.Page;

/**
 * Service interface for querying and managing reader's chapter reading history.
 */
public interface ReadingHistoryService {

    /**
     * Retrieves paginated reading history items for a user, sorted chronologically descending.
     *
     * @param email user email
     * @param page zero-based page index
     * @param size page size (capped up to 50 items as per BR-14)
     * @return page of ReadingHistoryItemDTO
     */
    Page<ReadingHistoryItemDTO> getReadingHistory(String email, int page, int size);

    /**
     * Records that the user has accessed (opened) a chapter by updating its last access time.
     * Does not change the chapter completion flag (isRead).
     *
     * @param email user email
     * @param chapterId chapter ID
     */
    void recordChapterAccess(String email, Integer chapterId);

    /**
     * Removes a specific chapter from the user's reading history without revoking VIP unlock.
     *
     * @param email user email
     * @param chapterId chapter ID
     */
    void removeHistoryItem(String email, Integer chapterId);

    /**
     * Clears all reading history records for the authenticated user.
     *
     * @param email user email
     */
    void clearHistory(String email);
}
