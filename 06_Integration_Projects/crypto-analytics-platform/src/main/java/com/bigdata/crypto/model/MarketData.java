package com.bigdata.crypto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "market_data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketData {
    
    @Id
    private String id;
    
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
    
    @JsonProperty("open")
    private BigDecimal open;
    
    @JsonProperty("close")
    private BigDecimal close;
    
    @JsonProperty("change")
    private BigDecimal change;
    
    @JsonProperty("changePercent")
    private BigDecimal changePercent;
    
    @JsonProperty("quoteVolume")
    private BigDecimal quoteVolume;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    private LocalDateTime createdAt;
    
    public MarketData() {
        this.createdAt = LocalDateTime.now();
    }
    
    public MarketData(String symbol, BigDecimal price, BigDecimal volume) {
        this();
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public BigDecimal getOpen() {
        return open;
    }
    
    public void setOpen(BigDecimal open) {
        this.open = open;
    }
    
    public BigDecimal getClose() {
        return close;
    }
    
    public void setClose(BigDecimal close) {
        this.close = close;
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
    
    public BigDecimal getQuoteVolume() {
        return quoteVolume;
    }
    
    public void setQuoteVolume(BigDecimal quoteVolume) {
        this.quoteVolume = quoteVolume;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "MarketData{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", volume=" + volume +
                ", timestamp=" + timestamp +
                '}';
    }
}
