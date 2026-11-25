package com.hkust.marketplace.models;

/**
 * lifecycle states for a listing
 */
public enum ListingStatus {
    PENDING_REVIEW,
    LIVE,
    REJECTED,
    RESERVED,
    SOLD,
    LENT,
    GIVEN,
    REMOVED
}
