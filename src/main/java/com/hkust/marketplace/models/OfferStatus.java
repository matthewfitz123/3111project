package com.hkust.marketplace.models;

/**
 * lifecycle states for an offer
 */
public enum OfferStatus {
    PENDING, // submitted by the buyer but the seller has not responded
    ACCEPTED,// seller accepted
    DECLINED,//seller rejected
    COUNTERED//seller proposed some different terms
}
