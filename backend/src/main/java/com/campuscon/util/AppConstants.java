package com.campuscon.util;

/**
 * Constants used throughout the application
 */
public class AppConstants {
    /**
     * Default page number for pagination
     */
    public static final String DEFAULT_PAGE_NUMBER = "0";
    
    /**
     * Default page size for pagination
     */
    public static final String DEFAULT_PAGE_SIZE = "10";
    
    /**
     * Default sort direction
     */
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    
    /**
     * Default sort field
     */
    public static final String DEFAULT_SORT_BY = "createdAt";
    
    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}
