package com.campuscon.repository;

import com.campuscon.model.College;
import com.campuscon.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
    List<College> findByUniversity(University university);
    Optional<College> findByNameAndUniversity(String name, University university);
}
