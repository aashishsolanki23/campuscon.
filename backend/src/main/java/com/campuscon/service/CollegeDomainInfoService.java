package com.campuscon.service;

import com.campuscon.model.CollegeDomainInfo;
import com.campuscon.repository.CollegeDomainInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeDomainInfoService {
    private final CollegeDomainInfoRepository collegeDomainInfoRepository;
    private final RestTemplate restTemplate;
    
    @Value("${google.sheet.colleges.url}")
    private String googleSheetPublishedUrl;

    /**
     * Synchronizes the college domain data from the Google Sheet to the database.
     * This method can be called manually or scheduled to run periodically.
     */
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void syncCollegeDomainData() {
        log.info("Starting synchronization with Google Sheet for college domains");
        try {
            // Fetch data from Google Sheet published as CSV or JSON
            String sheetData = restTemplate.getForObject(googleSheetPublishedUrl, String.class);
            List<CollegeDomainInfo> collegeInfoList = parseGoogleSheetData(sheetData);
            
            // Update database with fresh data
            updateCollegeDomainDatabase(collegeInfoList);
            
            log.info("Successfully synchronized college domain data from Google Sheet");
        } catch (Exception e) {
            log.error("Failed to synchronize college domain data", e);
        }
    }
    
    /**
     * Parses the data received from Google Sheet.
     * This method would be implemented based on the format of the published sheet
     * (CSV, JSON, etc.)
     */
    private List<CollegeDomainInfo> parseGoogleSheetData(String sheetData) {
        List<CollegeDomainInfo> collegeInfoList = new ArrayList<>();
        
        // Simple CSV parsing (assuming the Google Sheet is published as CSV)
        // Skip header row
        String[] rows = sheetData.split("\\n");
        for (int i = 1; i < rows.length; i++) {
            String[] columns = rows[i].split(",");
            if (columns.length >= 3) {
                CollegeDomainInfo info = CollegeDomainInfo.builder()
                        .collegeName(columns[0].trim())
                        .universityName(columns[1].trim())
                        .emailDomain(columns[2].trim())
                        .build();
                
                // Add optional fields if available
                if (columns.length > 3) info.setLocation(columns[3].trim());
                if (columns.length > 4) info.setAbbreviation(columns[4].trim());
                if (columns.length > 5) info.setWebsiteUrl(columns[5].trim());
                if (columns.length > 6) info.setCollegeCode(columns[6].trim());
                
                collegeInfoList.add(info);
            }
        }
        
        return collegeInfoList;
    }
    
    /**
     * Updates the database with the latest data from Google Sheet
     */
    private void updateCollegeDomainDatabase(List<CollegeDomainInfo> collegeInfoList) {
        for (CollegeDomainInfo newInfo : collegeInfoList) {
            Optional<CollegeDomainInfo> existingInfo = 
                collegeDomainInfoRepository.findByCollegeNameAndUniversityName(
                    newInfo.getCollegeName(), 
                    newInfo.getUniversityName()
                );
            
            if (existingInfo.isPresent()) {
                // Update existing entry
                CollegeDomainInfo info = existingInfo.get();
                info.setEmailDomain(newInfo.getEmailDomain());
                info.setLocation(newInfo.getLocation());
                info.setAbbreviation(newInfo.getAbbreviation());
                info.setWebsiteUrl(newInfo.getWebsiteUrl());
                info.setCollegeCode(newInfo.getCollegeCode());
                collegeDomainInfoRepository.save(info);
            } else {
                // Create new entry
                collegeDomainInfoRepository.save(newInfo);
            }
        }
    }
    
    /**
     * Validates an email domain against the college domain database
     */
    public boolean validateEmailDomain(String email, String collegeName, String universityName) {
        String domain = extractDomainFromEmail(email);
        
        Optional<CollegeDomainInfo> collegeInfo = 
            collegeDomainInfoRepository.findByCollegeNameAndUniversityName(collegeName, universityName);
            
        if (collegeInfo.isPresent()) {
            return domain.equalsIgnoreCase(collegeInfo.get().getEmailDomain());
        }
        
        return false;
    }
    
    /**
     * Get college information from an email domain
     */
    public Optional<CollegeDomainInfo> getCollegeInfoFromEmail(String email) {
        String domain = extractDomainFromEmail(email);
        return collegeDomainInfoRepository.findByEmailDomain(domain);
    }
    
    /**
     * Helper method to extract domain from email
     */
    private String extractDomainFromEmail(String email) {
        return email.substring(email.indexOf('@') + 1);
    }
    
    /**
     * Get all college domains in the system
     */
    public List<CollegeDomainInfo> getAllCollegeDomains() {
        return collegeDomainInfoRepository.findAll();
    }
    
    /**
     * Get college domains for a specific university
     */
    public List<CollegeDomainInfo> getCollegeDomainsByUniversity(String universityName) {
        return collegeDomainInfoRepository.findByUniversityName(universityName);
    }
    
    /**
     * Manually trigger college domain data synchronization
     */
    public void manualSyncCollegeDomainData() {
        syncCollegeDomainData();
    }
}
