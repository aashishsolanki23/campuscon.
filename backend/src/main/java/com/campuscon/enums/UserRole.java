package com.campuscon.enums;

/**
 * Represents the user types in the CampusCon system
 * In the simplified user model, users can have multiple types
 * and are identified by their user_types collection in the database
 */
public enum UserRole {
    USER,       // Regular user (default role for all users)
    ADMIN,      // System administrator
    MODERATOR   // Content moderator
}
