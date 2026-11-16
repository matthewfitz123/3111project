package com.hkust.marketplace.models;

import java.time.LocalDateTime;

public class Report {
    private String id;
    private String listingId;
    private String reporterUsername;    // who flagged it (usually a buyer)
    private String reason;              // why — shown to admin
    private LocalDateTime createdAt;
    private boolean resolved;           // admin marks handled in Manage Listings

    public Report() {
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
