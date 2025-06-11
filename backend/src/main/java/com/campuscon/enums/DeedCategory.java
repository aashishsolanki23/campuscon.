package com.campuscon.enums;

/**
 * Enumeration of different categories for deeds in the system.
 * This represents all possible categories of deeds in the CampusCon platform.
 */
public enum DeedCategory {
    // Academic categories
    TECHNICAL("Technical"),
    ACADEMIC("Academic"),
    SEMINAR("Seminar"),
    WORKSHOP("Workshop"),
    
    // Event categories
    EVENT("Event"),
    COMPETITION("Competition"),
    ANNOUNCEMENT("Announcement"),
    RECRUITMENT("Recruitment"),
    
    // Activity categories 
    CULTURAL("Cultural"),
    SPORTS("Sports"),
    SOCIAL("Social"),
    
    // Specific categories
    ARTICLE_WRITING("Article Writing"),
    AWARDS("Awards"),
    BUSINESS_PLAN("Business Plan"),
    CAMPS("Camps"),
    CONCLAVE("Conclave"),
    CASE_STUDY("Case Study"),
    INSTITUTION_FESTIVAL("Institution Festival"),
    CONFERENCE("Conference"),
    CODING_CHALLENGE("Coding Challenge"),
    DANCE("Dance"),
    DATA_SCIENCE("Data Science"),
    DATA_ANALYTICS("Data Analytics"),
    DEBATES("Debates"),
    DESIGNING("Designing"),
    ENTREPRENEURSHIP("Entrepreneurship"),
    FASHION("Fashion"),
    FELLOWSHIP("Fellowship"),
    FINANCE("Finance"),
    HUMAN_RESOURCE("Human Resource"),
    HACKATHON("Hackathon"),
    LITERARY("Literary"),
    MARKETING("Marketing"),
    MUSIC("Music"),
    ON_CAMPUS_QUIZ("On-Campus Quiz"),
    OPERATIONS("Operations"),
    PANEL_PRESENTATION("Panel Presentation"),
    PHOTOGRAPHY("Photography"),
    POSTER_MAKING("Poster Making"),
    PRESENTATION("Presentation"),
    ROBOTICS("Robotics"),
    SCHOLARSHIP("Scholarship"),
    SIMULATION_GAME("Simulation Game"),
    STARTUP_FAIR("Startup Fair"),
    STRATEGY("Strategy"),
    OTHER("Other");
    
    private final String displayName;
    
    DeedCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convert a display name to the corresponding enum value
     * @param displayName the display name to convert
     * @return the corresponding enum value or null if not found
     */
    public static DeedCategory fromDisplayName(String displayName) {
        for (DeedCategory category : DeedCategory.values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return OTHER;
    }
    
    /**
     * Get all category display names as an array
     * @return array of category display names
     */
    public static String[] getAllCategoryDisplayNames() {
        DeedCategory[] categories = DeedCategory.values();
        String[] displayNames = new String[categories.length];
        
        for (int i = 0; i < categories.length; i++) {
            displayNames[i] = categories[i].getDisplayName();
        }
        
        return displayNames;
    }
}
