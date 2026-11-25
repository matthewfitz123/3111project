package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

/**
 * buyer reviews on completed deals (step 22), seller replies and flags (step 23),
 * and admin hide/unhide (step 24). each completed deal can be rated once and
 * the seller's rating is recalculated from reviews that are still visible
 */
public final class ReviewService {

    public static final int MAX_COMMENT_LENGTH = 500;

    private ReviewService() {
        // static service
    }

    public static synchronized Review submitReview(Deal deal, User buyer, int rating, String comment) {
        User current = SessionManager.requireRole(UserRole.BUYER);
        if (buyer == null || !buyer.getUsername().equalsIgnoreCase(current.getUsername())) {
            throw new SecurityException("you can only review as yourself");
        }
        Deal fresh = deal == null ? null : DummyDataService.getInstance().findDeal(deal.getId());
        if (fresh == null) {
            throw new IllegalArgumentException("that deal no longer exists");
        }
        if (fresh.getBuyerUsername() == null
                || !fresh.getBuyerUsername().equalsIgnoreCase(current.getUsername())) {
            throw new SecurityException("that deal is not yours");
        }
        if (fresh.getStatus() != DealStatus.COMPLETED) {
            throw new IllegalStateException("only completed deals can be reviewed");
        }
        if (fresh.isRated()) {
            throw new IllegalStateException("you already reviewed this deal");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("pick a rating from 1 to 5 stars");
        }
        String text = comment == null ? "" : comment.trim();
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("keep the comment under " + MAX_COMMENT_LENGTH + " characters");
        }

        Review review = new Review();
        review.setDealId(fresh.getId());
        review.setSellerUsername(fresh.getSellerUsername());
        review.setBuyerUsername(current.getUsername());
        review.setListingTitle(fresh.getListingTitle());
        review.setRating(rating);
        review.setComment(text);
        DummyDataService.getInstance().addReview(review);

        fresh.setRated(true);
        DummyDataService.getInstance().recalculateSellerRating(fresh.getSellerUsername());
        NotificationService.reviewReceived(review);
        return review;
    }

    /**
     * seller answers a review on their own profile (step 23), sending again edits the reply
     */
    public static synchronized Review replyToReview(String reviewId, String reply) {
        Review review = requireOwnReview(reviewId);
        String text = reply == null ? "" : reply.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("write a reply first");
        }
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("keep the reply under " + MAX_COMMENT_LENGTH + " characters");
        }
        review.setSellerReply(text);
        NotificationService.reviewReplied(review);
        return review;
    }

    /**
     * seller reports a review, it is hidden until an admin checks it (step 23)
     */
    public static synchronized Review flagReview(String reviewId, String reason) {
        Review review = requireOwnReview(reviewId);
        if (review.isFlagged() || review.isHidden()) {
            throw new IllegalStateException("this review is already hidden for admin review");
        }
        String text = reason == null ? "" : reason.trim();
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("keep the reason under " + MAX_COMMENT_LENGTH + " characters");
        }
        review.setFlagged(true);
        review.setFlagReason(text);
        review.setHidden(true);
        DummyDataService.getInstance().recalculateSellerRating(review.getSellerUsername());
        NotificationService.reviewFlagged(review);
        return review;
    }

    /**
     * admin shows or hides a review (step 24). hidden reviews leave the public average.
     * unhiding also clears a seller flag so the review is public again
     */
    public static synchronized Review setHidden(String reviewId, boolean hidden) {
        SessionManager.requireRole(UserRole.ADMIN);
        Review review = reviewId == null ? null : DummyDataService.getInstance().findReview(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("that review no longer exists");
        }
        review.setHidden(hidden);
        if (!hidden) {
            review.setFlagged(false);
            review.setFlagReason(null);
        }
        DummyDataService.getInstance().recalculateSellerRating(review.getSellerUsername());
        return review;
    }

    private static Review requireOwnReview(String reviewId) {
        User seller = SessionManager.requireRole(UserRole.SELLER);
        Review review = reviewId == null ? null : DummyDataService.getInstance().findReview(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("that review no longer exists");
        }
        if (!review.getSellerUsername().equalsIgnoreCase(seller.getUsername())) {
            throw new SecurityException("that review is not about you");
        }
        return review;
    }
}
