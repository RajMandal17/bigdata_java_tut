package com.bigdata.app.model;

import com.bigdata.app.validation.ValidTransactionAmount;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing financial transactions
 * Demonstrates JPA mapping, validation, and indexing for Big Data scenarios
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_amount", columnList = "amount"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_status", columnList = "status")
})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    @NotBlank(message = "Customer ID is required")
    @Size(max = 50, message = "Customer ID must not exceed 50 characters")
    private String customerId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Amount is required")
    @ValidTransactionAmount
    private BigDecimal amount;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Column(length = 50)
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Column(length = 100)
    @Size(max = 100, message = "Merchant ID must not exceed 100 characters")
    private String merchantId;
    
    @Column(length = 200)
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    @Column(length = 3, nullable = false)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency = "USD";
    
    // Constructors
    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
    
    public Transaction(String customerId, BigDecimal amount, String category) {
        this();
        this.customerId = customerId;
        this.amount = amount;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", currency='" + currency + '\'' +
                '}';
    }
}
