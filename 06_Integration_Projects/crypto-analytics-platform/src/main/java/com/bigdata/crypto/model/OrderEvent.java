package com.bigdata.crypto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "order_events")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {
    
    @Id
    private String id;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("side")
    private String side; // BUY or SELL
    
    @JsonProperty("type")
    private String type; // LIMIT, MARKET, STOP
    
    @JsonProperty("status")
    private String status; // OPEN, FILLED, CANCELLED, PARTIAL
    
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("filledQuantity")
    private BigDecimal filledQuantity;
    
    @JsonProperty("remainingQuantity")
    private BigDecimal remainingQuantity;
    
    @JsonProperty("avgFillPrice")
    private BigDecimal avgFillPrice;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("timeInForce")
    private String timeInForce; // GTC, IOC, FOK
    
    @JsonProperty("stopPrice")
    private BigDecimal stopPrice;
    
    @JsonProperty("clientOrderId")
    private String clientOrderId;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    public OrderEvent() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public OrderEvent(String orderId, String symbol, String side, String type, 
                     BigDecimal quantity, BigDecimal price, String userId) {
        this();
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.userId = userId;
        this.timestamp = Instant.now();
        this.status = "OPEN";
        this.filledQuantity = BigDecimal.ZERO;
        this.remainingQuantity = quantity;
    }
    
    // Helper methods
    public boolean isFullyFilled() {
        return "FILLED".equals(status) && 
               filledQuantity != null && 
               quantity != null && 
               filledQuantity.compareTo(quantity) == 0;
    }
    
    public boolean isPartiallyFilled() {
        return filledQuantity != null && 
               filledQuantity.compareTo(BigDecimal.ZERO) > 0 && 
               !isFullyFilled();
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    public boolean isActive() {
        return "OPEN".equals(status) || "PARTIAL".equals(status);
    }
    
    public BigDecimal getFillPercentage() {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0 && filledQuantity != null) {
            return filledQuantity.divide(quantity, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getNotionalValue() {
        if (quantity != null && price != null) {
            return quantity.multiply(price);
        }
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSide() {
        return side;
    }
    
    public void setSide(String side) {
        this.side = side;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getFilledQuantity() {
        return filledQuantity;
    }
    
    public void setFilledQuantity(BigDecimal filledQuantity) {
        this.filledQuantity = filledQuantity;
        this.updatedAt = Instant.now();
        
        // Update remaining quantity
        if (quantity != null && filledQuantity != null) {
            this.remainingQuantity = quantity.subtract(filledQuantity);
        }
        
        // Update status based on fill
        if (filledQuantity != null && quantity != null) {
            if (filledQuantity.compareTo(quantity) == 0) {
                this.status = "FILLED";
            } else if (filledQuantity.compareTo(BigDecimal.ZERO) > 0) {
                this.status = "PARTIAL";
            }
        }
    }
    
    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }
    
    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
    
    public BigDecimal getAvgFillPrice() {
        return avgFillPrice;
    }
    
    public void setAvgFillPrice(BigDecimal avgFillPrice) {
        this.avgFillPrice = avgFillPrice;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTimeInForce() {
        return timeInForce;
    }
    
    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }
    
    public BigDecimal getStopPrice() {
        return stopPrice;
    }
    
    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }
    
    public String getClientOrderId() {
        return clientOrderId;
    }
    
    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", filledQuantity=" + filledQuantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
