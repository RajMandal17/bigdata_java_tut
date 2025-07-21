package com.bigdata.crypto.service;

import com.bigdata.crypto.model.OrderEvent;
import com.bigdata.crypto.model.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RealTimeMetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(RealTimeMetricsService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Real-time counters
    private final AtomicLong totalTradesCount = new AtomicLong(0);
    private final AtomicLong totalOrdersCount = new AtomicLong(0);
    private final AtomicReference<BigDecimal> totalVolumeUSDT = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicLong activeUsersCount = new AtomicLong(0);
    
    private static final String METRICS_KEY_PREFIX = "metrics:";
    private static final String TRADES_COUNT_KEY = "trades:count:";
    private static final String VOLUME_KEY = "volume:";
    private static final String ACTIVE_USERS_KEY = "users:active:";
    
    /**
     * Update metrics from trade event
     */
    public void updateTradeMetrics(TradeEvent tradeEvent) {
        try {
            String symbol = tradeEvent.getSymbol();
            String today = LocalDate.now().toString();
            
            // Update global counters
            totalTradesCount.incrementAndGet();
            
            // Update volume
            if (tradeEvent.getAmount() != null) {
                totalVolumeUSDT.updateAndGet(current -> current.add(tradeEvent.getAmount()));
            }
            
            // Update Redis metrics
            String tradesKey = TRADES_COUNT_KEY + symbol + ":" + today;
            String volumeKey = VOLUME_KEY + symbol + ":" + today;
            String globalTradesKey = TRADES_COUNT_KEY + "global:" + today;
            String globalVolumeKey = VOLUME_KEY + "global:" + today;
            
            // Increment trade counts
            redisTemplate.opsForValue().increment(tradesKey);
            redisTemplate.opsForValue().increment(globalTradesKey);
            redisTemplate.expire(tradesKey, Duration.ofDays(7));
            redisTemplate.expire(globalTradesKey, Duration.ofDays(7));
            
            // Update volumes
            if (tradeEvent.getAmount() != null) {
                redisTemplate.opsForHash().increment(volumeKey, "total", tradeEvent.getAmount().doubleValue());
                redisTemplate.opsForHash().increment(globalVolumeKey, "total", tradeEvent.getAmount().doubleValue());
                redisTemplate.expire(volumeKey, Duration.ofDays(7));
                redisTemplate.expire(globalVolumeKey, Duration.ofDays(7));
            }
            
            // Track active users
            if (tradeEvent.getUserId() != null) {
                String activeUsersKey = ACTIVE_USERS_KEY + today;
                redisTemplate.opsForSet().add(activeUsersKey, tradeEvent.getUserId());
                redisTemplate.expire(activeUsersKey, Duration.ofDays(1));
            }
            
            // Update price metrics
            updatePriceMetrics(symbol, tradeEvent.getPrice(), tradeEvent.getTimestamp());
            
            logger.debug("Updated trade metrics for symbol: {}, trade: {}", symbol, tradeEvent.getTradeId());
            
        } catch (Exception e) {
            logger.error("Error updating trade metrics: {}", tradeEvent, e);
        }
    }
    
    /**
     * Update metrics from order event
     */
    public void updateOrderMetrics(OrderEvent orderEvent) {
        try {
            String symbol = orderEvent.getSymbol();
            String today = LocalDate.now().toString();
            
            // Update global counters
            totalOrdersCount.incrementAndGet();
            
            // Update Redis metrics
            String ordersKey = "orders:count:" + symbol + ":" + today;
            String globalOrdersKey = "orders:count:global:" + today;
            String orderStatusKey = "orders:status:" + symbol + ":" + today;
            
            // Increment order counts
            redisTemplate.opsForValue().increment(ordersKey);
            redisTemplate.opsForValue().increment(globalOrdersKey);
            redisTemplate.expire(ordersKey, Duration.ofDays(7));
            redisTemplate.expire(globalOrdersKey, Duration.ofDays(7));
            
            // Track order status
            redisTemplate.opsForHash().increment(orderStatusKey, orderEvent.getStatus(), 1);
            redisTemplate.opsForHash().increment(orderStatusKey, orderEvent.getSide() + "_" + orderEvent.getType(), 1);
            redisTemplate.expire(orderStatusKey, Duration.ofDays(7));
            
            // Track active users
            if (orderEvent.getUserId() != null) {
                String activeUsersKey = ACTIVE_USERS_KEY + today;
                redisTemplate.opsForSet().add(activeUsersKey, orderEvent.getUserId());
                redisTemplate.expire(activeUsersKey, Duration.ofDays(1));
            }
            
            logger.debug("Updated order metrics for symbol: {}, order: {}", symbol, orderEvent.getOrderId());
            
        } catch (Exception e) {
            logger.error("Error updating order metrics: {}", orderEvent, e);
        }
    }
    
    /**
     * Get real-time trading metrics
     */
    public TradingMetrics getRealTimeMetrics() {
        try {
            String today = LocalDate.now().toString();
            String globalTradesKey = TRADES_COUNT_KEY + "global:" + today;
            String globalVolumeKey = VOLUME_KEY + "global:" + today;
            String activeUsersKey = ACTIVE_USERS_KEY + today;
            
            Long tradesCount = (Long) redisTemplate.opsForValue().get(globalTradesKey);
            Double volume = (Double) redisTemplate.opsForHash().get(globalVolumeKey, "total");
            Long activeUsers = redisTemplate.opsForSet().size(activeUsersKey);
            
            return TradingMetrics.builder()
                .totalTrades(tradesCount != null ? tradesCount : 0L)
                .totalVolumeUSDT(volume != null ? BigDecimal.valueOf(volume) : BigDecimal.ZERO)
                .activeUsers(activeUsers != null ? activeUsers : 0L)
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            logger.error("Error getting real-time metrics", e);
            return TradingMetrics.builder()
                .totalTrades(0L)
                .totalVolumeUSDT(BigDecimal.ZERO)
                .activeUsers(0L)
                .timestamp(Instant.now())
                .build();
        }
    }
    
    /**
     * Get symbol-specific metrics
     */
    public SymbolMetrics getSymbolMetrics(String symbol) {
        try {
            String today = LocalDate.now().toString();
            String tradesKey = TRADES_COUNT_KEY + symbol + ":" + today;
            String volumeKey = VOLUME_KEY + symbol + ":" + today;
            String orderStatusKey = "orders:status:" + symbol + ":" + today;
            String priceKey = "price:" + symbol;
            
            Long tradesCount = (Long) redisTemplate.opsForValue().get(tradesKey);
            Double volume = (Double) redisTemplate.opsForHash().get(volumeKey, "total");
            String currentPrice = (String) redisTemplate.opsForHash().get(priceKey, "current");
            String highPrice = (String) redisTemplate.opsForHash().get(priceKey, "high");
            String lowPrice = (String) redisTemplate.opsForHash().get(priceKey, "low");
            
            return SymbolMetrics.builder()
                .symbol(symbol)
                .tradesCount(tradesCount != null ? tradesCount : 0L)
                .volumeUSDT(volume != null ? BigDecimal.valueOf(volume) : BigDecimal.ZERO)
                .currentPrice(currentPrice != null ? new BigDecimal(currentPrice) : BigDecimal.ZERO)
                .highPrice(highPrice != null ? new BigDecimal(highPrice) : BigDecimal.ZERO)
                .lowPrice(lowPrice != null ? new BigDecimal(lowPrice) : BigDecimal.ZERO)
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            logger.error("Error getting symbol metrics for: {}", symbol, e);
            return SymbolMetrics.builder()
                .symbol(symbol)
                .tradesCount(0L)
                .volumeUSDT(BigDecimal.ZERO)
                .currentPrice(BigDecimal.ZERO)
                .timestamp(Instant.now())
                .build();
        }
    }
    
    private void updatePriceMetrics(String symbol, BigDecimal price, Instant timestamp) {
        try {
            if (price == null) return;
            
            String priceKey = "price:" + symbol;
            String today = LocalDate.now().toString();
            
            // Update current price
            redisTemplate.opsForHash().put(priceKey, "current", price.toString());
            redisTemplate.opsForHash().put(priceKey, "lastUpdate", timestamp.toString());
            
            // Update daily high/low
            String currentHigh = (String) redisTemplate.opsForHash().get(priceKey, "high");
            String currentLow = (String) redisTemplate.opsForHash().get(priceKey, "low");
            
            if (currentHigh == null || price.compareTo(new BigDecimal(currentHigh)) > 0) {
                redisTemplate.opsForHash().put(priceKey, "high", price.toString());
            }
            
            if (currentLow == null || price.compareTo(new BigDecimal(currentLow)) < 0) {
                redisTemplate.opsForHash().put(priceKey, "low", price.toString());
            }
            
            // Set TTL for price data
            redisTemplate.expire(priceKey, Duration.ofDays(1));
            
        } catch (Exception e) {
            logger.error("Error updating price metrics for symbol: {}, price: {}", symbol, price, e);
        }
    }
    
    // DTOs for metrics
    public static class TradingMetrics {
        private Long totalTrades;
        private BigDecimal totalVolumeUSDT;
        private Long activeUsers;
        private Instant timestamp;
        
        public static TradingMetricsBuilder builder() {
            return new TradingMetricsBuilder();
        }
        
        public static class TradingMetricsBuilder {
            private TradingMetrics metrics = new TradingMetrics();
            
            public TradingMetricsBuilder totalTrades(Long totalTrades) {
                metrics.totalTrades = totalTrades;
                return this;
            }
            
            public TradingMetricsBuilder totalVolumeUSDT(BigDecimal totalVolumeUSDT) {
                metrics.totalVolumeUSDT = totalVolumeUSDT;
                return this;
            }
            
            public TradingMetricsBuilder activeUsers(Long activeUsers) {
                metrics.activeUsers = activeUsers;
                return this;
            }
            
            public TradingMetricsBuilder timestamp(Instant timestamp) {
                metrics.timestamp = timestamp;
                return this;
            }
            
            public TradingMetrics build() {
                return metrics;
            }
        }
        
        // Getters
        public Long getTotalTrades() { return totalTrades; }
        public BigDecimal getTotalVolumeUSDT() { return totalVolumeUSDT; }
        public Long getActiveUsers() { return activeUsers; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public static class SymbolMetrics {
        private String symbol;
        private Long tradesCount;
        private BigDecimal volumeUSDT;
        private BigDecimal currentPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private Instant timestamp;
        
        public static SymbolMetricsBuilder builder() {
            return new SymbolMetricsBuilder();
        }
        
        public static class SymbolMetricsBuilder {
            private SymbolMetrics metrics = new SymbolMetrics();
            
            public SymbolMetricsBuilder symbol(String symbol) {
                metrics.symbol = symbol;
                return this;
            }
            
            public SymbolMetricsBuilder tradesCount(Long tradesCount) {
                metrics.tradesCount = tradesCount;
                return this;
            }
            
            public SymbolMetricsBuilder volumeUSDT(BigDecimal volumeUSDT) {
                metrics.volumeUSDT = volumeUSDT;
                return this;
            }
            
            public SymbolMetricsBuilder currentPrice(BigDecimal currentPrice) {
                metrics.currentPrice = currentPrice;
                return this;
            }
            
            public SymbolMetricsBuilder highPrice(BigDecimal highPrice) {
                metrics.highPrice = highPrice;
                return this;
            }
            
            public SymbolMetricsBuilder lowPrice(BigDecimal lowPrice) {
                metrics.lowPrice = lowPrice;
                return this;
            }
            
            public SymbolMetricsBuilder timestamp(Instant timestamp) {
                metrics.timestamp = timestamp;
                return this;
            }
            
            public SymbolMetrics build() {
                return metrics;
            }
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public Long getTradesCount() { return tradesCount; }
        public BigDecimal getVolumeUSDT() { return volumeUSDT; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public BigDecimal getHighPrice() { return highPrice; }
        public BigDecimal getLowPrice() { return lowPrice; }
        public Instant getTimestamp() { return timestamp; }
    }
}
