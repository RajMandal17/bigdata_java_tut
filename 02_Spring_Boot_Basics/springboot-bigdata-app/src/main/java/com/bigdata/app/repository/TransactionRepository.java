package com.bigdata.app.repository;

import com.bigdata.app.model.Transaction;
import com.bigdata.app.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction repository with optimized queries for Big Data scenarios
 * Demonstrates JPA query methods, custom queries, and pagination
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Simple query methods
    List<Transaction> findByCustomerId(String customerId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByCategory(String category);
    
    // Query with pagination
    Page<Transaction> findByCustomerId(String customerId, Pageable pageable);
    
    // Date range queries
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // Amount-based queries
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);
    
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Custom JPQL queries
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount AND t.status = :status")
    List<Transaction> findHighValueTransactions(
        @Param("amount") BigDecimal amount, 
        @Param("status") TransactionStatus status);
    
    @Query("SELECT t.category, COUNT(t), SUM(t.amount), AVG(t.amount) FROM Transaction t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY t.category " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getTransactionSummaryByCategory(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end);
    
    @Query("SELECT t.customerId, COUNT(t), SUM(t.amount) FROM Transaction t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY t.customerId " +
           "HAVING COUNT(t) > :minTransactions " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getTopCustomers(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("minTransactions") long minTransactions);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp > :since")
    long countRecentTransactionsByCustomer(
        @Param("customerId") String customerId,
        @Param("since") LocalDateTime since);
    
    // Native SQL query for complex analytics
    @Query(value = "SELECT " +
                   "DATE(timestamp) as transaction_date, " +
                   "COUNT(*) as transaction_count, " +
                   "SUM(amount) as total_amount, " +
                   "AVG(amount) as avg_amount " +
                   "FROM transactions " +
                   "WHERE timestamp >= :start AND timestamp <= :end " +
                   "GROUP BY DATE(timestamp) " +
                   "ORDER BY transaction_date", 
           nativeQuery = true)
    List<Object[]> getDailyTransactionStats(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
    
    // Fraud detection queries
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.amount > :unusualAmount " +
           "AND t.timestamp > :recentTime")
    List<Transaction> findSuspiciousTransactions(
        @Param("customerId") String customerId,
        @Param("unusualAmount") BigDecimal unusualAmount,
        @Param("recentTime") LocalDateTime recentTime);
}
