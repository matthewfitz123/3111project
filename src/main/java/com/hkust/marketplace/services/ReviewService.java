package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

/**
 * buyer reviews on completed deals (step 22)
 * each completed deal can be rated once and the seller's rating is recalculated
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
}
