package com.campuscon.enums;

/**
 * Represents the status of a student's registration for a deed
 */
public enum RegistrationStatus {
    PENDING,    // Waiting for approval (if approval is required)
    APPROVED,   // Approved by society
    REJECTED,   // Rejected by society
    CANCELLED   // Cancelled by student
}
