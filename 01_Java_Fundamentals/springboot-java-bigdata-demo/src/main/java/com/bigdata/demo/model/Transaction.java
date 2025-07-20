package com.bigdata.demo.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity demonstrating JPA mapping for Big Data scenarios
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_amount", columnList = "amount"),
    @Index(name = "idx_transaction_date", columnList = "transactionDate")
})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String customerId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Column(length = 100)
    private String merchantId;
    
    @Column(length = 3, nullable = false)
    private String currency = "USD";
    
    // Constructors
    public Transaction() {}
    
    public Transaction(String customerId, BigDecimal amount, String category, 
                      String description, String merchantId) {
        this.customerId = customerId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.merchantId = merchantId;
        this.transactionDate = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
    
    // Constructor with currency and date for demo purposes
    public Transaction(String id, BigDecimal amount, String currency, java.util.Date date) {
        this.customerId = id;
        this.amount = amount;
        this.currency = currency;
        this.transactionDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        this.status = TransactionStatus.COMPLETED;
        this.category = "DEMO";
        this.description = "Demo transaction";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", transactionDate=" + transactionDate +
                ", status=" + status +
                '}';
    }
}

enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
