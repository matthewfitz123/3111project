package com.hkust.marketplace.models;

import java.time.LocalDateTime;

public class ChatMessage {
    private String id;
    private String threadId;            // which conversation this belongs to
    private String senderUsername;      // who sent it
    private String content;             // the text
    private LocalDateTime sentAt;

    // formal offer support ("Make Offer" button inside chat):
    private boolean isFormalOffer;      // true if this message represents an Offer
    private String offerId;             // links to the Offer record, null for normal text

    public ChatMessage() {
        this.sentAt = LocalDateTime.now();
        this.isFormalOffer = false;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isFormalOffer() { return isFormalOffer; }
    public void setFormalOffer(boolean formalOffer) { isFormalOffer = formalOffer; }

    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
}
