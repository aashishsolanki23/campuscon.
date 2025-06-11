package com.campuscon.enums;

/**
 * Represents the status of content (Deeds)
 * Used for content moderation and visibility control
 */
public enum ContentStatus {
    PENDING,    // Waiting for moderation
    APPROVED,   // Approved and visible to users
    REJECTED,   // Rejected by moderation
    DELETED     // Deleted by the user or admin
}
