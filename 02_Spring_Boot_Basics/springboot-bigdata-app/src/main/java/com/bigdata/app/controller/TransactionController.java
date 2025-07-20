package com.bigdata.app.controller;

import com.bigdata.app.model.ProcessingResult;
import com.bigdata.app.model.Transaction;
import com.bigdata.app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Transaction REST Controller
 * Demonstrates REST API best practices for Big Data applications
 */
@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * Create a new transaction
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        logger.info("Creating transaction for customer: {}", transaction.getCustomerId());
        Transaction saved = transactionService.saveTransaction(transaction);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Process batch of transactions
     */
    @PostMapping("/batch")
    public ResponseEntity<String> processBatch(@RequestBody List<@Valid Transaction> transactions) {
        if (transactions.size() > 10000) {
            return ResponseEntity.badRequest()
                .body("Batch size cannot exceed 10,000 transactions");
        }
        
        logger.info("Processing batch of {} transactions", transactions.size());
        CompletableFuture<ProcessingResult> future = transactionService.processBatchTransactions(transactions);
        return ResponseEntity.ok("Batch processing initiated with " + transactions.size() + " transactions");
    }
    
    /**
     * Get transactions by customer with pagination
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<Transaction>> getCustomerTransactions(
            @PathVariable @NotBlank String customerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Transaction> transactions = transactionService.getTransactionsByCustomer(customerId, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get high value transactions
     */
    @GetMapping("/high-value")
    public ResponseEntity<List<Transaction>> getHighValueTransactions(
            @RequestParam BigDecimal threshold) {
        List<Transaction> transactions = transactionService.getHighValueTransactions(threshold);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction analytics for date range
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Object> analytics = transactionService.getTransactionAnalytics(start, end);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Detect suspicious transactions for a customer
     */
    @GetMapping("/suspicious/{customerId}")
    public ResponseEntity<List<Transaction>> getSuspiciousTransactions(
            @PathVariable @NotBlank String customerId) {
        List<Transaction> suspicious = transactionService.detectSuspiciousTransactions(customerId);
        return ResponseEntity.ok(suspicious);
    }
    
    /**
     * Generate sample data for testing
     */
    @PostMapping("/generate-sample")
    public ResponseEntity<ProcessingResult> generateSampleData(
            @RequestParam @Min(1) @Max(100000) int count) {
        ProcessingResult result = transactionService.generateSampleData(count);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "TransactionController"
        );
        return ResponseEntity.ok(health);
    }
}
