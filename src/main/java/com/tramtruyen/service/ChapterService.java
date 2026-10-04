package com.tramtruyen.service;

import com.tramtruyen.dto.ChapterForm;
import com.tramtruyen.entity.Chapter;

import java.util.List;

/** Business operations for chapter management. */
public interface ChapterService {

    List<Chapter> findNovelChapters(Integer novelId);

    Chapter createChapter(Integer novelId, ChapterForm form);

    Chapter getEditableChapter(Integer novelId, Integer chapterId);

    Chapter updateChapter(Integer novelId, Integer chapterId, ChapterForm form);

    void deleteChapter(Integer novelId, Integer chapterId);

    Chapter getPublicChapter(Integer novelId, Integer chapterNumber);

    List<Chapter> getPublicChapters(Integer novelId);
}