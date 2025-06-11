package com.campuscon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.campuscon.dto.ApiResponse;

import java.time.LocalDateTime;

/**
 * This controller provides simplified college-related functionality.
 * All detailed college and university functionality has been removed and replaced with a
 * simple collegeName string approach for user profiles.
 */
@RestController
@RequestMapping("/api/v1/colleges")
public class CollegeController {
    
    /**
     * Returns a message indicating that detailed college functionality has been simplified
     * 
     * @return ResponseEntity with information about the college name approach
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> collegeEndpoint() {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .message("The system now uses a simplified collegeName string for user profiles.")
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
