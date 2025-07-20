package com.bigdata.demo.repository;

import com.bigdata.demo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository demonstrating JPA queries for Big Data scenarios
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Method name queries
    List<Transaction> findByCustomerId(String customerId);
    
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);
    
    List<Transaction> findByCategory(String category);
    
    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Custom JPQL queries for analytics
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold ORDER BY t.amount DESC")
    List<Transaction> findHighValueTransactions(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customerId = :customerId")
    long countTransactionsByCustomer(@Param("customerId") String customerId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.customerId = :customerId")
    BigDecimal getTotalAmountByCustomer(@Param("customerId") String customerId);
    
    // Native SQL for complex analytics
    @Query(value = "SELECT t.category, " +
                   "COUNT(*) as transaction_count, " +
                   "SUM(t.amount) as total_amount, " +
                   "AVG(t.amount) as avg_amount, " +
                   "MIN(t.amount) as min_amount, " +
                   "MAX(t.amount) as max_amount " +
                   "FROM transactions t " +
                   "WHERE t.transaction_date >= :startDate " +
                   "GROUP BY t.category " +
                   "ORDER BY total_amount DESC", nativeQuery = true)
    List<Object[]> getAnalyticsByCategory(@Param("startDate") LocalDateTime startDate);
    
    @Query(value = "SELECT t.customer_id, " +
                   "COUNT(*) as transaction_count, " +
                   "SUM(t.amount) as total_spent, " +
                   "AVG(t.amount) as avg_transaction " +
                   "FROM transactions t " +
                   "WHERE t.transaction_date >= :startDate " +
                   "GROUP BY t.customer_id " +
                   "HAVING COUNT(*) >= :minTransactions " +
                   "ORDER BY total_spent DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopCustomers(@Param("startDate") LocalDateTime startDate,
                                  @Param("minTransactions") int minTransactions,
                                  @Param("limit") int limit);
}
