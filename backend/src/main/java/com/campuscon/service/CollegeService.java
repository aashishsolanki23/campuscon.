package com.campuscon.service;

import com.campuscon.model.College;
import com.campuscon.model.University;
import com.campuscon.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {
    private final CollegeRepository collegeRepository;
    private final UniversityService universityService;
    
    public List<College> getCollegesByUniversityId(Long universityId) {
        University university = universityService.getUniversityById(universityId);
        return collegeRepository.findByUniversity(university);
    }
    
    public College getCollegeById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("College not found"));
    }
    
    public College createCollege(College college, Long universityId) {
        University university = universityService.getUniversityById(universityId);
        college.setUniversity(university);
        return collegeRepository.save(college);
    }
    
    public boolean validateEmailDomain(String email, Long collegeId) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        
        String emailDomain = email.substring(email.indexOf('@') + 1);
        College college = getCollegeById(collegeId);
        
        // Get the expected domain for this college
        String expectedDomain = college.getFullEmailDomain();
        
        // Compare the domains (case-insensitive)
        return emailDomain.equalsIgnoreCase(expectedDomain);
    }
}
