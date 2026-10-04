package com.tramtruyen.service;

import com.tramtruyen.dto.BookshelfPageDTO;
import com.tramtruyen.dto.UserProfileDTO;
import com.tramtruyen.entity.Bookshelf;

import java.util.List;

/**
 * Service interface for personal bookshelf operations (M2-F07, M2-F08, M2-F06).
 */
public interface BookshelfService {

    List<Bookshelf> getUserBookshelf(String email);

    BookshelfPageDTO getBookshelfPage(String email, String filter, String query, String sort, int page, int size);

    void addNovelToBookshelf(String email, Integer novelId);

    void removeNovelFromBookshelf(String email, Integer novelId);

    boolean isNovelInBookshelf(String email, Integer novelId);

    void markNovelAsRead(String email, Integer novelId);

    void recordChapterRead(String email, Integer chapterId);

    UserProfileDTO getUserProfile(String email);

    long countNovelFavorites(Integer novelId);
}
