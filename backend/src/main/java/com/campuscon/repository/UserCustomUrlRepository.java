package com.campuscon.repository;

import com.campuscon.model.UserCustomUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCustomUrlRepository extends JpaRepository<UserCustomUrl, Long> {
    
    List<UserCustomUrl> findByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM UserCustomUrl u WHERE u.user.id = :userId")
    void deleteByUserId(Long userId);
}
