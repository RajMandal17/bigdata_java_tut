package com.bigdata.foundations.mysql.repository;

import com.bigdata.foundations.mysql.entity.User;
import com.bigdata.foundations.mysql.entity.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic finder methods
    Optional<User> findByEmail(String email);
    
    List<User> findByStatus(UserStatus status);
    
    List<User> findByCountry(String country);
    
    // Pagination support
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    // Complex queries with @Query
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByRegistrationDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.city = :city AND u.status = :status")
    List<User> findUsersByCityAndStatus(
        @Param("city") String city, 
        @Param("status") UserStatus status);
    
    // Native SQL queries for complex analytics
    @Query(value = """
        SELECT 
            country,
            COUNT(*) as user_count,
            COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users
        FROM users 
        WHERE created_at >= :fromDate
        GROUP BY country
        ORDER BY user_count DESC
        """, nativeQuery = true)
    List<Object[]> getUserStatsByCountry(@Param("fromDate") LocalDateTime fromDate);
    
    @Query(value = """
        SELECT 
            DATE(created_at) as registration_date,
            COUNT(*) as daily_registrations
        FROM users 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY DATE(created_at)
        ORDER BY registration_date
        """, nativeQuery = true)
    List<Object[]> getDailyRegistrationStats(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    // Bulk operations
    @Modifying
    @Query("UPDATE User u SET u.status = :newStatus WHERE u.status = :oldStatus")
    int bulkUpdateUserStatus(
        @Param("oldStatus") UserStatus oldStatus,
        @Param("newStatus") UserStatus newStatus);
    
    @Modifying
    @Query("UPDATE User u SET u.status = 'INACTIVE' WHERE u.updatedAt < :cutoffDate")
    int deactivateInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count queries
    long countByStatus(UserStatus status);
    
    long countByCountryAndStatus(String country, UserStatus status);
    
    // Exists queries
    boolean existsByEmail(String email);
}
