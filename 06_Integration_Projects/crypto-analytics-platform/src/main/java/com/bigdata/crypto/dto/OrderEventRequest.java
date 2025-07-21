package com.bigdata.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public class OrderEventRequest {
    
    @NotBlank(message = "Order ID is required")
    @JsonProperty("orderId")
    private String orderId;
    
    @NotBlank(message = "Symbol is required")
    @JsonProperty("symbol")
    private String symbol;
    
    @NotBlank(message = "Side is required")
    @JsonProperty("side")
    private String side;
    
    @NotBlank(message = "Type is required")
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("status")
    private String status;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("filledQuantity")
    private BigDecimal filledQuantity;
    
    @JsonProperty("avgFillPrice")
    private BigDecimal avgFillPrice;
    
    @NotBlank(message = "User ID is required")
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("timeInForce")
    private String timeInForce;
    
    @JsonProperty("stopPrice")
    private BigDecimal stopPrice;
    
    @JsonProperty("clientOrderId")
    private String clientOrderId;
    
    public OrderEventRequest() {}
    
    public OrderEventRequest(String orderId, String symbol, String side, String type,
                           BigDecimal quantity, BigDecimal price, String userId) {
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
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "OrderEventRequest{" +
                "orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
