package com.campuscon.service;

import com.campuscon.model.College;
import com.campuscon.model.University;
import com.campuscon.repository.CollegeRepository;
import com.campuscon.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuzzyMatchService {
    
    private final UniversityRepository universityRepository;
    private final CollegeRepository collegeRepository;
    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
    
    private static final double SIMILARITY_THRESHOLD = 0.85;
    
    /**
     * Find a matching university by name using fuzzy matching
     * @param name the university name to search for
     * @return Optional containing the matching university if found
     */
    public Optional<University> findMatchingUniversity(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedName = normalizeName(name);
        List<University> allUniversities = universityRepository.findAll();
        
        University bestMatch = null;
        double bestScore = 0.0;
        
        for (University university : allUniversities) {
            double score = similarity.apply(normalizedName, normalizeName(university.getName()));
            if (score > bestScore && score >= SIMILARITY_THRESHOLD) {
                bestScore = score;
                bestMatch = university;
            }
        }
        
        if (bestMatch != null) {
            log.info("Found fuzzy match for university '{}' -> '{}' (score: {})", 
                    name, bestMatch.getName(), bestScore);
        }
        
        return Optional.ofNullable(bestMatch);
    }
    
    /**
     * Find a matching college by name using fuzzy matching
     * @param name the college name to search for
     * @param university the university context (optional)
     * @return Optional containing the matching college if found
     */
    public Optional<College> findMatchingCollege(String name, University university) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedName = normalizeName(name);
        List<College> collegesToSearch;
        
        if (university != null) {
            // Search within the specific university
            collegesToSearch = collegeRepository.findByUniversity(university);
        } else {
            // Search all colleges
            collegesToSearch = collegeRepository.findAll();
        }
        
        College bestMatch = null;
        double bestScore = 0.0;
        
        for (College college : collegesToSearch) {
            double score = similarity.apply(normalizedName, normalizeName(college.getName()));
            if (score > bestScore && score >= SIMILARITY_THRESHOLD) {
                bestScore = score;
                bestMatch = college;
            }
        }
        
        if (bestMatch != null) {
            log.info("Found fuzzy match for college '{}' -> '{}' (score: {})", 
                    name, bestMatch.getName(), bestScore);
        }
        
        return Optional.ofNullable(bestMatch);
    }
    
    /**
     * Normalize a name for better matching
     * @param name the name to normalize
     * @return normalized name
     */
    private String normalizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }
    
    /**
     * Check if two names are similar enough to be considered the same
     * @param name1 first name
     * @param name2 second name
     * @return true if names are similar enough
     */
    public boolean areNamesSimilar(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }
        
        double score = similarity.apply(normalizeName(name1), normalizeName(name2));
        return score >= SIMILARITY_THRESHOLD;
    }
}
