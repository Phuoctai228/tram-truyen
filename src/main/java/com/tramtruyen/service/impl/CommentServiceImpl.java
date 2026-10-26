package com.tramtruyen.service.impl;

import com.tramtruyen.entity.Comment;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.CommentRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final com.tramtruyen.repository.UserChapterActivityRepository userChapterActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getVisibleCommentsByNovel(Integer novelId) {
        if (novelId == null) {
            return Collections.emptyList();
        }
        return commentRepository.findByNovelIdAndParentIsNullAndStatusOrderByCreatedAtDesc(novelId, "VISIBLE");
    }

    @Override
    @Transactional
    public Comment addNovelComment(String email, Integer novelId, Integer parentId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));

        long readCount = userChapterActivityRepository.countByUserAndChapter_NovelAndIsReadTrue(user, novel);
        if (readCount <= 0) {
            throw new IllegalArgumentException("Bạn cần đọc ít nhất 1 chương của bộ truyện này trước khi tham gia bình luận thảo luận!");
        }

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận gốc cần trả lời"));
            // If parent has a parent, attach to the top-level parent to keep Facebook 2-tier structure
            if (parent.getParent() != null) {
                parent = parent.getParent();
            }
        }

        Comment comment = Comment.builder()
                .user(user)
                .novel(novel)
                .parent(parent)
                .content(content.trim())
                .status("VISIBLE")
                .build();

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment updateComment(String email, Integer commentId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(content.trim());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(String email, Integer commentId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        // Only the owner can delete their own comment
        boolean isOwner = comment.getUser() != null && comment.getUser().getId().equals(user.getId());

        if (!isOwner) {
            throw new IllegalArgumentException("Bạn chỉ có thể xóa bình luận của chính mình");
        }

        commentRepository.delete(comment);
    }
}
