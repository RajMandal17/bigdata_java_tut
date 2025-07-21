package com.bigdata.crypto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "order_books")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBook {
    
    @Id
    private String id;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("bids")
    private List<OrderBookEntry> bids;
    
    @JsonProperty("asks")
    private List<OrderBookEntry> asks;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("sequence")
    private Long sequence;
    
    private Instant createdAt;
    
    public OrderBook() {
        this.createdAt = Instant.now();
    }
    
    public OrderBook(String symbol, List<OrderBookEntry> bids, List<OrderBookEntry> asks) {
        this();
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
        this.timestamp = Instant.now();
    }
    
    // Helper methods
    public BigDecimal getBestBidPrice() {
        return bids != null && !bids.isEmpty() ? bids.get(0).getPrice() : BigDecimal.ZERO;
    }
    
    public BigDecimal getBestAskPrice() {
        return asks != null && !asks.isEmpty() ? asks.get(0).getPrice() : BigDecimal.ZERO;
    }
    
    public BigDecimal getSpread() {
        BigDecimal bestBid = getBestBidPrice();
        BigDecimal bestAsk = getBestAskPrice();
        if (bestBid.compareTo(BigDecimal.ZERO) > 0 && bestAsk.compareTo(BigDecimal.ZERO) > 0) {
            return bestAsk.subtract(bestBid);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getSpreadPercentage() {
        BigDecimal spread = getSpread();
        BigDecimal bestBid = getBestBidPrice();
        if (spread.compareTo(BigDecimal.ZERO) > 0 && bestBid.compareTo(BigDecimal.ZERO) > 0) {
            return spread.divide(bestBid, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalBidVolume() {
        return bids != null ? bids.stream()
                .map(OrderBookEntry::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalAskVolume() {
        return asks != null ? asks.stream()
                .map(OrderBookEntry::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
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
    
    public List<OrderBookEntry> getBids() {
        return bids;
    }
    
    public void setBids(List<OrderBookEntry> bids) {
        this.bids = bids;
    }
    
    public List<OrderBookEntry> getAsks() {
        return asks;
    }
    
    public void setAsks(List<OrderBookEntry> asks) {
        this.asks = asks;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getSequence() {
        return sequence;
    }
    
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "OrderBook{" +
                "symbol='" + symbol + '\'' +
                ", bidsCount=" + (bids != null ? bids.size() : 0) +
                ", asksCount=" + (asks != null ? asks.size() : 0) +
                ", bestBid=" + getBestBidPrice() +
                ", bestAsk=" + getBestAskPrice() +
                ", spread=" + getSpread() +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // Inner class for order book entries
    public static class OrderBookEntry {
        @JsonProperty("price")
        private BigDecimal price;
        
        @JsonProperty("size")
        private BigDecimal size;
        
        @JsonProperty("num-orders")
        private Integer numOrders;
        
        public OrderBookEntry() {}
        
        public OrderBookEntry(BigDecimal price, BigDecimal size) {
            this.price = price;
            this.size = size;
        }
        
        public OrderBookEntry(BigDecimal price, BigDecimal size, Integer numOrders) {
            this.price = price;
            this.size = size;
            this.numOrders = numOrders;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public BigDecimal getSize() {
            return size;
        }
        
        public void setSize(BigDecimal size) {
            this.size = size;
        }
        
        public Integer getNumOrders() {
            return numOrders;
        }
        
        public void setNumOrders(Integer numOrders) {
            this.numOrders = numOrders;
        }
        
        public BigDecimal getValue() {
            return price != null && size != null ? price.multiply(size) : BigDecimal.ZERO;
        }
        
        @Override
        public String toString() {
            return "OrderBookEntry{" +
                    "price=" + price +
                    ", size=" + size +
                    ", numOrders=" + numOrders +
                    '}';
        }
    }
}
