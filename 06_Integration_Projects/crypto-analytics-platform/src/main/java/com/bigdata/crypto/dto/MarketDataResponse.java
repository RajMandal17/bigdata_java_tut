package com.bigdata.crypto.dto;

import com.bigdata.crypto.model.OrderBook;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketDataResponse {
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("volume")
    private BigDecimal volume;
    
    @JsonProperty("high")
    private BigDecimal high;
    
    @JsonProperty("low")
    private BigDecimal low;
    
    @JsonProperty("change")
    private BigDecimal change;
    
    @JsonProperty("changePercent")
    private BigDecimal changePercent;
    
    @JsonProperty("orderBook")
    private OrderBook orderBook;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    public MarketDataResponse() {}
    
    public MarketDataResponse(String symbol, BigDecimal price, BigDecimal volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = Instant.now();
    }
    
    // Builder pattern
    public static MarketDataResponseBuilder builder() {
        return new MarketDataResponseBuilder();
    }
    
    public static class MarketDataResponseBuilder {
        private MarketDataResponse response = new MarketDataResponse();
        
        public MarketDataResponseBuilder symbol(String symbol) {
            response.symbol = symbol;
            return this;
        }
        
        public MarketDataResponseBuilder price(BigDecimal price) {
            response.price = price;
            return this;
        }
        
        public MarketDataResponseBuilder volume(BigDecimal volume) {
            response.volume = volume;
            return this;
        }
        
        public MarketDataResponseBuilder high(BigDecimal high) {
            response.high = high;
            return this;
        }
        
        public MarketDataResponseBuilder low(BigDecimal low) {
            response.low = low;
            return this;
        }
        
        public MarketDataResponseBuilder change(BigDecimal change) {
            response.change = change;
            return this;
        }
        
        public MarketDataResponseBuilder changePercent(BigDecimal changePercent) {
            response.changePercent = changePercent;
            return this;
        }
        
        public MarketDataResponseBuilder orderBook(OrderBook orderBook) {
            response.orderBook = orderBook;
            return this;
        }
        
        public MarketDataResponseBuilder timestamp(Instant timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        
        public MarketDataResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getVolume() {
        return volume;
    }
    
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }
    
    public BigDecimal getHigh() {
        return high;
    }
    
    public void setHigh(BigDecimal high) {
        this.high = high;
    }
    
    public BigDecimal getLow() {
        return low;
    }
    
    public void setLow(BigDecimal low) {
        this.low = low;
    }
    
    public BigDecimal getChange() {
        return change;
    }
    
    public void setChange(BigDecimal change) {
        this.change = change;
    }
    
    public BigDecimal getChangePercent() {
        return changePercent;
    }
    
    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }
    
    public OrderBook getOrderBook() {
        return orderBook;
    }
    
    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "MarketDataResponse{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", volume=" + volume +
                ", change=" + change +
                ", changePercent=" + changePercent +
                ", timestamp=" + timestamp +
                '}';
    }
}
