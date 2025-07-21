package com.bigdata.crypto.service;

import com.bigdata.crypto.model.OrderBook;
import com.bigdata.crypto.model.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderBookSnapshotService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderBookSnapshotService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // In-memory cache for latest order book snapshots
    private final ConcurrentHashMap<String, OrderBook> orderBookCache = new ConcurrentHashMap<>();
    
    private static final String REDIS_KEY_PREFIX = "orderbook:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    /**
     * Update order book from trade event
     */
    public void updateFromTrade(TradeEvent tradeEvent) {
        try {
            String symbol = tradeEvent.getSymbol();
            String redisKey = REDIS_KEY_PREFIX + symbol;
            
            // Get current order book from Redis or cache
            OrderBook currentOrderBook = getCurrentOrderBook(symbol);
            
            if (currentOrderBook != null) {
                // Update order book based on trade
                OrderBook updatedOrderBook = applyTradeToOrderBook(currentOrderBook, tradeEvent);
                
                // Save to cache
                orderBookCache.put(symbol, updatedOrderBook);
                
                // Save to Redis with TTL
                redisTemplate.opsForValue().set(redisKey, updatedOrderBook, CACHE_TTL);
                
                logger.debug("Updated order book for symbol: {} from trade: {}", 
                           symbol, tradeEvent.getTradeId());
            } else {
                logger.warn("No current order book found for symbol: {}, cannot update from trade", symbol);
            }
            
        } catch (Exception e) {
            logger.error("Error updating order book from trade: {}", tradeEvent, e);
        }
    }
    
    /**
     * Get current order book snapshot for a symbol
     */
    public OrderBook getCurrentOrderBook(String symbol) {
        try {
            // Check in-memory cache first
            OrderBook cached = orderBookCache.get(symbol);
            if (cached != null && isRecentSnapshot(cached)) {
                return cached;
            }
            
            // Try Redis
            String redisKey = REDIS_KEY_PREFIX + symbol;
            OrderBook fromRedis = (OrderBook) redisTemplate.opsForValue().get(redisKey);
            if (fromRedis != null) {
                orderBookCache.put(symbol, fromRedis);
                return fromRedis;
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error getting current order book for symbol: {}", symbol, e);
            return null;
        }
    }
    
    /**
     * Set order book snapshot (typically from API fetch)
     */
    public void setOrderBookSnapshot(String symbol, OrderBook orderBook) {
        try {
            String redisKey = REDIS_KEY_PREFIX + symbol;
            
            // Update timestamp
            orderBook.setTimestamp(Instant.now());
            
            // Save to cache
            orderBookCache.put(symbol, orderBook);
            
            // Save to Redis with TTL
            redisTemplate.opsForValue().set(redisKey, orderBook, CACHE_TTL);
            
            logger.debug("Set order book snapshot for symbol: {}", symbol);
            
        } catch (Exception e) {
            logger.error("Error setting order book snapshot for symbol: {}", symbol, e);
        }
    }
    
    /**
     * Get order book statistics
     */
    public OrderBookStats getOrderBookStats(String symbol) {
        OrderBook orderBook = getCurrentOrderBook(symbol);
        if (orderBook == null) {
            return null;
        }
        
        return OrderBookStats.builder()
            .symbol(symbol)
            .bestBidPrice(orderBook.getBestBidPrice())
            .bestAskPrice(orderBook.getBestAskPrice())
            .spread(orderBook.getSpread())
            .spreadPercentage(orderBook.getSpreadPercentage())
            .totalBidVolume(orderBook.getTotalBidVolume())
            .totalAskVolume(orderBook.getTotalAskVolume())
            .bidCount(orderBook.getBids() != null ? orderBook.getBids().size() : 0)
            .askCount(orderBook.getAsks() != null ? orderBook.getAsks().size() : 0)
            .timestamp(orderBook.getTimestamp())
            .build();
    }
    
    /**
     * Clear order book cache for a symbol
     */
    public void clearOrderBookCache(String symbol) {
        try {
            orderBookCache.remove(symbol);
            String redisKey = REDIS_KEY_PREFIX + symbol;
            redisTemplate.delete(redisKey);
            
            logger.debug("Cleared order book cache for symbol: {}", symbol);
            
        } catch (Exception e) {
            logger.error("Error clearing order book cache for symbol: {}", symbol, e);
        }
    }
    
    private OrderBook applyTradeToOrderBook(OrderBook orderBook, TradeEvent tradeEvent) {
        // This is a simplified update - in reality, you'd need more sophisticated logic
        // to properly update the order book based on trade execution
        
        // For now, just update the timestamp to indicate activity
        OrderBook updated = new OrderBook();
        updated.setSymbol(orderBook.getSymbol());
        updated.setBids(orderBook.getBids());
        updated.setAsks(orderBook.getAsks());
        updated.setSequence(orderBook.getSequence() != null ? orderBook.getSequence() + 1 : 1L);
        updated.setTimestamp(tradeEvent.getTimestamp());
        
        return updated;
    }
    
    private boolean isRecentSnapshot(OrderBook orderBook) {
        if (orderBook.getTimestamp() == null) {
            return false;
        }
        
        Duration age = Duration.between(orderBook.getTimestamp(), Instant.now());
        return age.compareTo(Duration.ofMinutes(1)) < 0; // Consider recent if less than 1 minute old
    }
    
    // Inner class for order book statistics
    public static class OrderBookStats {
        private String symbol;
        private BigDecimal bestBidPrice;
        private BigDecimal bestAskPrice;
        private BigDecimal spread;
        private BigDecimal spreadPercentage;
        private BigDecimal totalBidVolume;
        private BigDecimal totalAskVolume;
        private int bidCount;
        private int askCount;
        private Instant timestamp;
        
        public static OrderBookStatsBuilder builder() {
            return new OrderBookStatsBuilder();
        }
        
        public static class OrderBookStatsBuilder {
            private OrderBookStats stats = new OrderBookStats();
            
            public OrderBookStatsBuilder symbol(String symbol) {
                stats.symbol = symbol;
                return this;
            }
            
            public OrderBookStatsBuilder bestBidPrice(BigDecimal bestBidPrice) {
                stats.bestBidPrice = bestBidPrice;
                return this;
            }
            
            public OrderBookStatsBuilder bestAskPrice(BigDecimal bestAskPrice) {
                stats.bestAskPrice = bestAskPrice;
                return this;
            }
            
            public OrderBookStatsBuilder spread(BigDecimal spread) {
                stats.spread = spread;
                return this;
            }
            
            public OrderBookStatsBuilder spreadPercentage(BigDecimal spreadPercentage) {
                stats.spreadPercentage = spreadPercentage;
                return this;
            }
            
            public OrderBookStatsBuilder totalBidVolume(BigDecimal totalBidVolume) {
                stats.totalBidVolume = totalBidVolume;
                return this;
            }
            
            public OrderBookStatsBuilder totalAskVolume(BigDecimal totalAskVolume) {
                stats.totalAskVolume = totalAskVolume;
                return this;
            }
            
            public OrderBookStatsBuilder bidCount(int bidCount) {
                stats.bidCount = bidCount;
                return this;
            }
            
            public OrderBookStatsBuilder askCount(int askCount) {
                stats.askCount = askCount;
                return this;
            }
            
            public OrderBookStatsBuilder timestamp(Instant timestamp) {
                stats.timestamp = timestamp;
                return this;
            }
            
            public OrderBookStats build() {
                return stats;
            }
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public BigDecimal getBestBidPrice() { return bestBidPrice; }
        public BigDecimal getBestAskPrice() { return bestAskPrice; }
        public BigDecimal getSpread() { return spread; }
        public BigDecimal getSpreadPercentage() { return spreadPercentage; }
        public BigDecimal getTotalBidVolume() { return totalBidVolume; }
        public BigDecimal getTotalAskVolume() { return totalAskVolume; }
        public int getBidCount() { return bidCount; }
        public int getAskCount() { return askCount; }
        public Instant getTimestamp() { return timestamp; }
    }
}
