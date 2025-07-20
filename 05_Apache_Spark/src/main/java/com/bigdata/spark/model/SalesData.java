package com.bigdata.spark.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales Data Model representing sales transaction data
 * Used for Spark DataFrame operations and analytics
 */
public class SalesData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("product_id")
    private String productId;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @JsonProperty("transaction_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;
    
    @JsonProperty("customer_age")
    private Integer customerAge;
    
    @JsonProperty("customer_gender")
    private String customerGender;
    
    @JsonProperty("customer_location")
    private String customerLocation;
    
    @JsonProperty("payment_method")
    private String paymentMethod;

    // Default constructor
    public SalesData() {}

    // Constructor with all fields
    public SalesData(String transactionId, String customerId, String productId, 
                    String productName, String category, Integer quantity, 
                    BigDecimal unitPrice, BigDecimal totalAmount, 
                    LocalDateTime transactionDate, Integer customerAge, 
                    String customerGender, String customerLocation, String paymentMethod) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.customerAge = customerAge;
        this.customerGender = customerGender;
        this.customerLocation = customerLocation;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public Integer getCustomerAge() { return customerAge; }
    public void setCustomerAge(Integer customerAge) { this.customerAge = customerAge; }

    public String getCustomerGender() { return customerGender; }
    public void setCustomerGender(String customerGender) { this.customerGender = customerGender; }

    public String getCustomerLocation() { return customerLocation; }
    public void setCustomerLocation(String customerLocation) { this.customerLocation = customerLocation; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @Override
    public String toString() {
        return "SalesData{" +
                "transactionId='" + transactionId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", transactionDate=" + transactionDate +
                ", customerAge=" + customerAge +
                ", customerGender='" + customerGender + '\'' +
                ", customerLocation='" + customerLocation + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
