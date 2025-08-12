package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.model.College;
import com.campuscon.model.University;
import com.campuscon.service.FuzzyMatchService;
import com.campuscon.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
@Slf4j
public class InstitutionController {
    
    private final InstitutionService institutionService;
    private final FuzzyMatchService fuzzyMatchService;
    
    /**
     * Search for universities with fuzzy matching
     */
    @GetMapping("/universities/search")
    public ResponseEntity<ApiResponse<List<University>>> searchUniversities(@RequestParam String query) {
        try {
            List<University> universities = institutionService.searchUniversities(query);
            return ResponseEntity.ok(ApiResponse.success(universities));
        } catch (Exception e) {
            log.error("Error searching universities", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to search universities"));
        }
    }
    
    /**
     * Search for colleges with fuzzy matching
     */
    @GetMapping("/colleges/search")
    public ResponseEntity<ApiResponse<List<College>>> searchColleges(
            @RequestParam String query,
            @RequestParam(required = false) Long universityId) {
        try {
            List<College> colleges = institutionService.searchColleges(query, universityId);
            return ResponseEntity.ok(ApiResponse.success(colleges));
        } catch (Exception e) {
            log.error("Error searching colleges", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to search colleges"));
        }
    }
    
    /**
     * Get all universities
     */
    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<List<University>>> getAllUniversities() {
        try {
            List<University> universities = institutionService.getAllUniversities();
            return ResponseEntity.ok(ApiResponse.success(universities));
        } catch (Exception e) {
            log.error("Error fetching universities", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch universities"));
        }
    }
    
    /**
     * Get colleges by university
     */
    @GetMapping("/universities/{universityId}/colleges")
    public ResponseEntity<ApiResponse<List<College>>> getCollegesByUniversity(@PathVariable Long universityId) {
        try {
            List<College> colleges = institutionService.getCollegesByUniversity(universityId);
            return ResponseEntity.ok(ApiResponse.success(colleges));
        } catch (Exception e) {
            log.error("Error fetching colleges for university {}", universityId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fetch colleges"));
        }
    }
    
    /**
     * Fuzzy match university name
     */
    @GetMapping("/universities/fuzzy-match")
    public ResponseEntity<ApiResponse<University>> fuzzyMatchUniversity(@RequestParam String name) {
        try {
            var university = fuzzyMatchService.findMatchingUniversity(name);
            if (university.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(university.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "No matching university found"));
            }
        } catch (Exception e) {
            log.error("Error fuzzy matching university", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fuzzy match university"));
        }
    }
    
    /**
     * Fuzzy match college name
     */
    @GetMapping("/colleges/fuzzy-match")
    public ResponseEntity<ApiResponse<College>> fuzzyMatchCollege(
            @RequestParam String name,
            @RequestParam(required = false) Long universityId) {
        try {
            University university = null;
            if (universityId != null) {
                university = institutionService.getUniversityById(universityId);
            }
            
            var college = fuzzyMatchService.findMatchingCollege(name, university);
            if (college.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(college.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "No matching college found"));
            }
        } catch (Exception e) {
            log.error("Error fuzzy matching college", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to fuzzy match college"));
        }
    }
}
