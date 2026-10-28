package com.tramtruyen.service;

import com.tramtruyen.dto.ChapterAccessDTO;
import com.tramtruyen.entity.Chapter;
import com.tramtruyen.entity.Novel;
import org.springframework.security.core.Authentication;

/**
 * Service evaluating visitor access rights, masking content, and providing top-up recommendations for a chapter.
 */
public interface ChapterAccessService {

    /**
     * Evaluates access permissions, preview paragraphs, and shortfall packages for a chapter.
     *
     * @param novel novel entity
     * @param chapter chapter entity
     * @param authentication current security authentication context
     * @return ChapterAccessDTO containing complete access resolution
     */
    ChapterAccessDTO checkAccess(Novel novel, Chapter chapter, Authentication authentication);
}
