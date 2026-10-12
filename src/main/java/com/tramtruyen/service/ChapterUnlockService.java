package com.tramtruyen.service;

import com.tramtruyen.dto.UnlockResultDTO;

/**
 * Service interface for handling paid VIP chapter unlocking and access validation.
 */
public interface ChapterUnlockService {

    /**
     * Checks if a specific chapter is already unlocked by the authenticated user.
     *
     * @param email user email
     * @param chapterId chapter ID
     * @return true if unlocked or if chapter is free (price = 0)
     */
    boolean isChapterUnlocked(String email, Integer chapterId);

    /**
     * Performs transactional chapter unlock by deducting coins and granting permanent access.
     *
     * @param email user email
     * @param chapterId chapter ID
     * @param autoUnlockNext flag whether reader opted in to auto-unlock following chapters
     * @return UnlockResultDTO containing transaction outcome
     */
    UnlockResultDTO unlockChapter(String email, Integer chapterId, Boolean autoUnlockNext);
}
