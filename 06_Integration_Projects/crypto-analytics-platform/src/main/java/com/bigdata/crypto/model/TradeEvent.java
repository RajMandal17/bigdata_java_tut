package com.bigdata.crypto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Document(collection = "trade_events")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeEvent {
    
    @Id
    private String id;
    
    @JsonProperty("tradeId")
    private String tradeId;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("side")
    private String side; // BUY or SELL
    
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("buyOrderId")
    private String buyOrderId;
    
    @JsonProperty("sellOrderId")
    private String sellOrderId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("userAddress")
    private String userAddress;
    
    @JsonProperty("fee")
    private BigDecimal fee;
    
    @JsonProperty("feeCurrency")
    private String feeCurrency;
    
    private MarketData marketData;
    private Map<String, Object> metadata;
    private Instant createdAt;
    
    public TradeEvent() {
        this.createdAt = Instant.now();
    }
    
    public TradeEvent(String tradeId, String symbol, String side, BigDecimal quantity, BigDecimal price) {
        this();
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.amount = quantity.multiply(price);
        this.timestamp = Instant.now();
    }
    
    // Builder pattern
    public static TradeEventBuilder builder() {
        return new TradeEventBuilder();
    }
    
    public static class TradeEventBuilder {
        private TradeEvent tradeEvent = new TradeEvent();
        
        public TradeEventBuilder tradeId(String tradeId) {
            tradeEvent.tradeId = tradeId;
            return this;
        }
        
        public TradeEventBuilder symbol(String symbol) {
            tradeEvent.symbol = symbol;
            return this;
        }
        
        public TradeEventBuilder side(String side) {
            tradeEvent.side = side;
            return this;
        }
        
        public TradeEventBuilder quantity(BigDecimal quantity) {
            tradeEvent.quantity = quantity;
            return this;
        }
        
        public TradeEventBuilder price(BigDecimal price) {
            tradeEvent.price = price;
            return this;
        }
        
        public TradeEventBuilder amount(BigDecimal amount) {
            tradeEvent.amount = amount;
            return this;
        }
        
        public TradeEventBuilder timestamp(Instant timestamp) {
            tradeEvent.timestamp = timestamp;
            return this;
        }
        
        public TradeEventBuilder buyOrderId(String buyOrderId) {
            tradeEvent.buyOrderId = buyOrderId;
            return this;
        }
        
        public TradeEventBuilder sellOrderId(String sellOrderId) {
            tradeEvent.sellOrderId = sellOrderId;
            return this;
        }
        
        public TradeEventBuilder userId(String userId) {
            tradeEvent.userId = userId;
            return this;
        }
        
        public TradeEventBuilder userAddress(String userAddress) {
            tradeEvent.userAddress = userAddress;
            return this;
        }
        
        public TradeEventBuilder marketData(MarketData marketData) {
            tradeEvent.marketData = marketData;
            return this;
        }
        
        public TradeEventBuilder metadata(Map<String, Object> metadata) {
            tradeEvent.metadata = metadata;
            return this;
        }
        
        public TradeEvent build() {
            if (tradeEvent.amount == null && tradeEvent.quantity != null && tradeEvent.price != null) {
                tradeEvent.amount = tradeEvent.quantity.multiply(tradeEvent.price);
            }
            return tradeEvent;
        }
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    
    public String getUserAddress() {
        return userAddress;
    }
    
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
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
    
    public MarketData getMarketData() {
        return marketData;
    }
    
    public void setMarketData(MarketData marketData) {
        this.marketData = marketData;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "TradeEvent{" +
                "tradeId='" + tradeId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
