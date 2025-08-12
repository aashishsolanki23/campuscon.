package com.campuscon.repository;

import com.campuscon.model.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    
    Optional<OTP> findByEmailAndOtpCodeAndTypeAndUsedFalse(String email, String otpCode, OTP.OTPType type);
    
    List<OTP> findByEmailAndTypeAndUsedFalse(String email, OTP.OTPType type);
    
    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.type = :type AND o.used = false AND o.expiryTime > :now ORDER BY o.createdAt DESC")
    List<OTP> findValidOTPsByEmailAndType(@Param("email") String email, @Param("type") OTP.OTPType type, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE OTP o SET o.used = true WHERE o.email = :email AND o.type = :type AND o.used = false")
    void markAllOTPsAsUsed(@Param("email") String email, @Param("type") OTP.OTPType type);
    
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.expiryTime < :now")
    void deleteExpiredOTPs(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(o) FROM OTP o WHERE o.email = :email AND o.type = :type AND o.createdAt > :since")
    long countRecentOTPsByEmailAndType(@Param("email") String email, @Param("type") OTP.OTPType type, @Param("since") LocalDateTime since);
}
