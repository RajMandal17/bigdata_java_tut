package com.bigdata.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public class TradeEventRequest {
    
    @NotBlank(message = "Trade ID is required")
    @JsonProperty("tradeId")
    private String tradeId;
    
    @NotBlank(message = "Symbol is required")
    @JsonProperty("symbol")
    private String symbol;
    
    @NotBlank(message = "Side is required")
    @JsonProperty("side")
    private String side;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("buyOrderId")
    private String buyOrderId;
    
    @JsonProperty("sellOrderId")
    private String sellOrderId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("fee")
    private BigDecimal fee;
    
    @JsonProperty("feeCurrency")
    private String feeCurrency;
    
    public TradeEventRequest() {}
    
    public TradeEventRequest(String tradeId, String symbol, String side, 
                           BigDecimal quantity, BigDecimal price) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getBuyOrderId() {
        return buyOrderId;
    }
    
    public void setBuyOrderId(String buyOrderId) {
        this.buyOrderId = buyOrderId;
    }
    
    public String getSellOrderId() {
        return sellOrderId;
    }
    
    public void setSellOrderId(String sellOrderId) {
        this.sellOrderId = sellOrderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
    
    public String getFeeCurrency() {
        return feeCurrency;
    }
    
    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }
    
    @Override
    public String toString() {
        return "TradeEventRequest{" +
                "tradeId='" + tradeId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}
