package com.tramtruyen.service;

import com.tramtruyen.dto.NovelForm;
import com.tramtruyen.entity.Novel;

import java.util.List;

/** Business operations for internal novel management. */
public interface NovelService {

    List<Novel> findAdminNovels(String query, String status);

    Novel getEditableNovel(Integer id);

    Novel createNovel(NovelForm form);

    Novel updateNovel(Integer id, NovelForm form);

    void deleteNovel(Integer id);
}