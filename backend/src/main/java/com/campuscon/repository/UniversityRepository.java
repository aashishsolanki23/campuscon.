package com.campuscon.repository;

import com.campuscon.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    
    Optional<University> findByNameIgnoreCase(String name);
    
    @Query("SELECT u FROM University u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<University> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    List<University> findByCountryIgnoreCase(String country);
    
    List<University> findByStateIgnoreCase(String state);
    
    List<University> findByCityIgnoreCase(String city);
    
    @Query("SELECT u FROM University u WHERE u.name LIKE %:searchTerm% OR u.city LIKE %:searchTerm% OR u.state LIKE %:searchTerm%")
    List<University> searchUniversities(@Param("searchTerm") String searchTerm);
}
