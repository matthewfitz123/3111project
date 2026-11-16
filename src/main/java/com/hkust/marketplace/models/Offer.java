package com.hkust.marketplace.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Offer {

    private String id;              // assigned by DummyDataService ("O1", "O2", ...)
    private String listingId;       // which listing this offer is on
    private String buyerUsername;   // who made the offer
    private String sellerUsername;  // who owns the listing (denormalized for easy lookup)
    private ListingType type;       // copied from the listing: SELL / LEND / GIVEAWAY
    private OfferStatus status;     // PENDING by default

    // proposal terms — only the relevant pair is used:
    private Double offerPrice;      // SELL: buyer's proposed price (Double, NOT double — must be nullable)
    private LocalDate borrowStart;  // LEND: proposed pickup date
    private LocalDate borrowEnd;    // LEND: proposed return date

    private String message;         // optional note, e.g. "Can pick up after 6pm"

    // seller's counter-terms, filled when status = COUNTERED:
    private Double counterPrice;    // SELL counter
    private LocalDate counterEnd;   // LEND counter (new return date)

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;  // null until seller acts

    public Offer() {
        this.status = OfferStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public ListingType getType() { return type; }
    public void setType(ListingType type) { this.type = type; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public Double getOfferPrice() { return offerPrice; }
    public void setOfferPrice(Double offerPrice) { this.offerPrice = offerPrice; }

    public LocalDate getBorrowStart() { return borrowStart; }
    public void setBorrowStart(LocalDate borrowStart) { this.borrowStart = borrowStart; }

    public LocalDate getBorrowEnd() { return borrowEnd; }
    public void setBorrowEnd(LocalDate borrowEnd) { this.borrowEnd = borrowEnd; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Double getCounterPrice() { return counterPrice; }
    public void setCounterPrice(Double counterPrice) { this.counterPrice = counterPrice; }

    public LocalDate getCounterEnd() { return counterEnd; }
    public void setCounterEnd(LocalDate counterEnd) { this.counterEnd = counterEnd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
