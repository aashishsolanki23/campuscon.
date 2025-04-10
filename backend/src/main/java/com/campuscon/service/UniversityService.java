package com.campuscon.service;

import com.campuscon.model.University;
import com.campuscon.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityService {
    private final UniversityRepository universityRepository;
    
    public List<University> getAllUniversities() {
        return universityRepository.findAll();
    }
    
    public University getUniversityById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("University not found"));
    }
    
    public University getUniversityByName(String name) {
        return universityRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("University not found"));
    }
    
    public University createUniversity(University university) {
        if (universityRepository.existsByEmailDomain(university.getEmailDomain())) {
            throw new RuntimeException("A university with this email domain already exists");
        }
        return universityRepository.save(university);
    }
}
