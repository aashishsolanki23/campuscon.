package com.campuscon.service;

import com.campuscon.model.College;
import com.campuscon.model.University;
import com.campuscon.repository.CollegeRepository;
import com.campuscon.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {
    
    private final UniversityRepository universityRepository;
    private final CollegeRepository collegeRepository;
    
    /**
     * Search universities by query
     */
    public List<University> searchUniversities(String query) {
        if (query == null || query.trim().isEmpty()) {
            return universityRepository.findAll();
        }
        return universityRepository.searchUniversities(query.trim());
    }
    
    /**
     * Search colleges by query and optionally by university
     */
    public List<College> searchColleges(String query, Long universityId) {
        if (query == null || query.trim().isEmpty()) {
            if (universityId != null) {
                return collegeRepository.findByUniversityId(universityId);
            }
            return collegeRepository.findAll();
        }
        
        if (universityId != null) {
            University university = getUniversityById(universityId);
            if (university != null) {
                return collegeRepository.findByUniversityAndNameContainingIgnoreCase(university, query.trim());
            }
        }
        
        return collegeRepository.searchColleges(query.trim());
    }
    
    /**
     * Get all universities
     */
    public List<University> getAllUniversities() {
        return universityRepository.findAll();
    }
    
    /**
     * Get colleges by university
     */
    public List<College> getCollegesByUniversity(Long universityId) {
        return collegeRepository.findByUniversityId(universityId);
    }
    
    /**
     * Get university by ID
     */
    public University getUniversityById(Long id) {
        Optional<University> university = universityRepository.findById(id);
        return university.orElse(null);
    }
    
    /**
     * Get college by ID
     */
    public College getCollegeById(Long id) {
        Optional<College> college = collegeRepository.findById(id);
        return college.orElse(null);
    }
    
    /**
     * Create or find university by name
     */
    public University createOrFindUniversity(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // First try to find existing university
        Optional<University> existing = universityRepository.findByNameIgnoreCase(name.trim());
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new university
        University university = new University();
        university.setName(name.trim());
        university = universityRepository.save(university);
        
        log.info("Created new university: {}", university.getName());
        return university;
    }
    
    /**
     * Create or find college by name and university
     */
    public College createOrFindCollege(String name, University university) {
        if (name == null || name.trim().isEmpty() || university == null) {
            return null;
        }
        
        // First try to find existing college within the university
        List<College> existingColleges = collegeRepository.findByUniversity(university);
        for (College college : existingColleges) {
            if (college.getName().equalsIgnoreCase(name.trim())) {
                return college;
            }
        }
        
        // Create new college
        College college = new College();
        college.setName(name.trim());
        college.setUniversity(university);
        college = collegeRepository.save(college);
        
        log.info("Created new college: {} in university: {}", college.getName(), university.getName());
        return college;
    }
}
