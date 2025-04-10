package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.model.CollegeDomainInfo;
import com.campuscon.service.CollegeDomainInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/college-domains")
@RequiredArgsConstructor
public class CollegeDomainInfoController {
    private final CollegeDomainInfoService collegeDomainInfoService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollegeDomainInfo>>> getAllCollegeDomains() {
        List<CollegeDomainInfo> domains = collegeDomainInfoService.getAllCollegeDomains();
        return ResponseEntity.ok(ApiResponse.success(domains, "College domains retrieved successfully"));
    }
    
    @GetMapping("/university/{universityName}")
    public ResponseEntity<ApiResponse<List<CollegeDomainInfo>>> getCollegeDomainsByUniversity(
            @PathVariable String universityName) {
        List<CollegeDomainInfo> domains = collegeDomainInfoService.getCollegeDomainsByUniversity(universityName);
        return ResponseEntity.ok(ApiResponse.success(domains, "College domains for university retrieved successfully"));
    }
    
    @PostMapping("/validate-email")
    public ResponseEntity<ApiResponse<Boolean>> validateEmailDomain(
            @RequestParam String email,
            @RequestParam String collegeName,
            @RequestParam String universityName) {
        Boolean isValid = collegeDomainInfoService.validateEmailDomain(email, collegeName, universityName);
        return ResponseEntity.ok(ApiResponse.success(isValid, "Email domain validation completed"));
    }
    
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> triggerManualSync() {
        collegeDomainInfoService.manualSyncCollegeDomainData();
        return ResponseEntity.ok(ApiResponse.success(null, "College domain data synchronization initiated"));
    }
}
