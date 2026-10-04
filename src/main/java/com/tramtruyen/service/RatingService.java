package com.tramtruyen.service;

import com.tramtruyen.entity.NovelRating;

/**
 * Service interface for novel ratings and reviews (M4-F10 / UC-10 / BR-17).
 */
public interface RatingService {

    /**
     * Submit or update a user rating for a novel.
     * Enforces BR-17: each member has 1 rating per novel; subsequent ratings overwrite earlier ones.
     * Recalculates average rating and rating count for the novel.
     */
    void submitRating(String email, Integer novelId, Integer rating, String reviewText);

    /**
     * Get a user's existing rating for a novel (if any).
     */
    NovelRating getUserRating(String email, Integer novelId);
}
