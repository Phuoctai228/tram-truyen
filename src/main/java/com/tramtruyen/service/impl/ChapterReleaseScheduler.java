package com.tramtruyen.service.impl;

import com.tramtruyen.service.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the scheduled VIP-to-free chapter transition.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChapterReleaseScheduler {

    private static final long RELEASE_CHECK_INTERVAL_MILLISECONDS = 60_000L;

    private final ChapterService chapterService;

    /**
     * Releases due VIP chapters without bypassing the chapter service layer.
     */
    @Scheduled(fixedDelay = RELEASE_CHECK_INTERVAL_MILLISECONDS)
    public void releaseDueChapters() {
        int releasedCount = chapterService.releaseDueVipChapters();
        if (releasedCount > 0) {
            log.info("Released {} scheduled VIP chapters as free chapters", releasedCount);
        }
    }
}
