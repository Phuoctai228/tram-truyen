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

        // BR-17: 1 review per user per novel; overwrite if already exists
        NovelRating ratingEntity = novelRatingRepository.findByNovelIdAndUserId(novel.getId(), user.getId())
                .orElseGet(() -> NovelRating.builder()
                        .novel(novel)
                        .user(user)
                        .build());

        ratingEntity.setRating(rating);
        ratingEntity.setReviewText(reviewText != null ? reviewText.trim() : null);
        novelRatingRepository.save(ratingEntity);

        // Recalculate average rating & rating count
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
}
