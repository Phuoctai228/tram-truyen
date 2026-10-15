package com.tramtruyen.service.impl;

import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.NovelRating;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.NovelRatingRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Production implementation of RatingService conforming to M4-F10 and BR-17.
 */
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final NovelRatingRepository novelRatingRepository;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void submitRating(String email, Integer novelId, Integer rating, String reviewText) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5 sao");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));

        // BR-17: Chỉ cho người dùng đánh giá truyện 1 lần. Nếu đã đánh giá, phải xóa mới được đánh giá lại.
        NovelRating existingRating = novelRatingRepository.findByNovelIdAndUserId(novel.getId(), user.getId())
                .orElse(null);
        if (existingRating != null) {
            throw new IllegalArgumentException("Bạn đã đánh giá bộ truyện này rồi. Vui lòng xóa đánh giá cũ nếu muốn đánh giá lại!");
        }

        NovelRating ratingEntity = NovelRating.builder()
                .novel(novel)
                .user(user)
                .rating(rating)
                .reviewText(reviewText != null ? reviewText.trim() : null)
                .build();
        novelRatingRepository.save(ratingEntity);

        // Recalculate average rating & rating count
        recalculateNovelRatingStats(novel);
    }

    @Override
    @Transactional
    public void deleteRating(String email, Integer novelId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));

        NovelRating existingRating = novelRatingRepository.findByNovelIdAndUserId(novel.getId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đánh giá bộ truyện này"));

        novelRatingRepository.delete(existingRating);

        // Recalculate average rating & rating count
        recalculateNovelRatingStats(novel);
    }

    @Override
    @Transactional
    public void updateRating(String email, Integer novelId, Integer rating, String reviewText) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5 sao");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện"));

        NovelRating existingRating = novelRatingRepository.findByNovelIdAndUserId(novel.getId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đánh giá bộ truyện này"));

        existingRating.setRating(rating);
        existingRating.setReviewText(reviewText != null ? reviewText.trim() : null);
        novelRatingRepository.save(existingRating);

        // Recalculate average rating & rating count
        recalculateNovelRatingStats(novel);
    }

    private void recalculateNovelRatingStats(Novel novel) {
        Double avgRating = novelRatingRepository.calculateAverageRatingByNovelId(novel.getId());
        long totalRatings = novelRatingRepository.countByNovelId(novel.getId());

        if (avgRating != null) {
            novel.setAverageRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        } else {
            novel.setAverageRating(BigDecimal.ZERO);
        }
        novel.setRatingCount((int) totalRatings);

        novelRepository.save(novel);
    }

    @Override
    @Transactional(readOnly = true)
    public NovelRating getUserRating(String email, Integer novelId) {
        if (email == null || novelId == null) {
            return null;
        }
        return userRepository.findByEmail(email)
                .flatMap(user -> novelRatingRepository.findByNovelIdAndUserId(novelId, user.getId()))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<NovelRating> getRatingsByNovel(Integer novelId) {
        if (novelId == null) {
            return java.util.Collections.emptyList();
        }
        return novelRatingRepository.findByNovelIdOrderByUpdatedAtDesc(novelId);
    }
}
