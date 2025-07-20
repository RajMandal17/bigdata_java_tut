package com.bigdata.kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("customer_id")
    private String customerId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("merchant_id")
    private String merchantId;
    
    @JsonProperty("merchant_name")
    private String merchantName;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("status")
    private TransactionStatus status;
    
    @JsonProperty("payment_method")
    private String paymentMethod;
    
    @JsonProperty("card_type")
    private String cardType;
    
    @JsonProperty("location")
    private LocationData location;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("risk_score")
    private Double riskScore;
    
    @JsonProperty("source_system")
    private String sourceSystem;
    
    public enum TransactionStatus {
        PENDING, APPROVED, DECLINED, FAILED, CANCELLED, REFUNDED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String country;
        private String city;
        private String zipCode;
        private Double latitude;
        private Double longitude;
        private String ipAddress;
    }
}
