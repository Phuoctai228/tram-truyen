package com.tramtruyen.service;

import com.tramtruyen.entity.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> getVisibleCommentsByNovel(Integer novelId);

    Comment addNovelComment(String email, Integer novelId, Integer parentId, String content);

    Comment updateComment(String email, Integer commentId, String content);

    void deleteComment(String email, Integer commentId);
}
