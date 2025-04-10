package com.campuscon.repository;

import com.campuscon.model.CollegeDomainInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeDomainInfoRepository extends JpaRepository<CollegeDomainInfo, Long> {
    Optional<CollegeDomainInfo> findByCollegeNameAndUniversityName(String collegeName, String universityName);
    Optional<CollegeDomainInfo> findByEmailDomain(String emailDomain);
    List<CollegeDomainInfo> findByUniversityName(String universityName);
    boolean existsByEmailDomain(String emailDomain);
}
