package com.hkust.marketplace.models;

/**
 * lifecycle states for a deal
 */
public enum DealStatus {
    PENDING, //offer accepted
    ACTIVE, // item borrowed/sold
    COMPLETED,// buyer confirmed they got it
    RETURNED, // borrowed item handed back
    CANCELLED, // deal called off
    EXPIRED // borrow period ended without return
}
