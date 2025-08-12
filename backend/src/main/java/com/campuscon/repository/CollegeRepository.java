package com.campuscon.repository;

import com.campuscon.model.College;
import com.campuscon.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
    
    Optional<College> findByNameIgnoreCase(String name);
    
    List<College> findByUniversity(University university);
    
    List<College> findByUniversityId(Long universityId);
    
    @Query("SELECT c FROM College c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<College> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM College c WHERE c.university = :university AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<College> findByUniversityAndNameContainingIgnoreCase(@Param("university") University university, @Param("searchTerm") String searchTerm);
    
    List<College> findByCityIgnoreCase(String city);
    
    List<College> findByStateIgnoreCase(String state);
    
    @Query("SELECT c FROM College c WHERE c.name LIKE %:searchTerm% OR c.city LIKE %:searchTerm% OR c.state LIKE %:searchTerm%")
    List<College> searchColleges(@Param("searchTerm") String searchTerm);
}
