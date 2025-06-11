package com.campuscon.enums;

/**
 * Enum representing the types of chat groups in the deed hierarchy.
 */
public enum DeedGroupType {
    MAIN("Main Group"),
    PARTICIPANT_TEAM("Team Chat"),
    PARTICIPANT_ALL("All Participants"),
    CREATOR_TEAM("Team Management"),
    CREATOR_ALL("Announcements");
    
    private final String displayName;
    
    DeedGroupType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
}
