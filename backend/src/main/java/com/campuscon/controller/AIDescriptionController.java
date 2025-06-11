package com.campuscon.controller;

import com.campuscon.dto.ai.AIDescriptionRequest;
import com.campuscon.dto.ai.AIDescriptionResponse;
import com.campuscon.service.AIDescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIDescriptionController {

    private final AIDescriptionService aiDescriptionService;

    @PostMapping("/generate-description")
    public ResponseEntity<AIDescriptionResponse> generateDescription(
            @Valid @RequestBody AIDescriptionRequest request) {
        
        try {
            log.info("Generating AI description for prompt: {}", request.getPrompt());
            
            String generatedDescription = aiDescriptionService.generateDescription(
                request.getPrompt(), 
                request.getDeedTitle(), 
                request.getCategory()
            );
            
            AIDescriptionResponse response = AIDescriptionResponse.builder()
                    .generatedDescription(generatedDescription)
                    .success(true)
                    .message("Description generated successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating AI description", e);
            
            AIDescriptionResponse response = AIDescriptionResponse.builder()
                    .generatedDescription("")
                    .success(false)
                    .message("Failed to generate description: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
