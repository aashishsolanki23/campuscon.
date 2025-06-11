package com.campuscon.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIDescriptionRequest {
    
    @NotBlank(message = "Prompt is required")
    @Size(max = 500, message = "Prompt must not exceed 500 characters")
    private String prompt;
    
    private String deedTitle;
    
    private String category;
}
