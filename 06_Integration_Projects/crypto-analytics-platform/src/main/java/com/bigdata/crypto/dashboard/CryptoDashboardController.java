package com.bigdata.crypto.dashboard;

import com.bigdata.crypto.service.RealTimeMetricsService;
import com.bigdata.crypto.service.OrderBookSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class CryptoDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoDashboardController.class);
    
    @Autowired
    private RealTimeMetricsService metricsService;
    
    @Autowired
    private OrderBookSnapshotService orderBookService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @GetMapping("/market-overview")
    public ResponseEntity<MarketOverview> getMarketOverview() {
        try {
            logger.info("Fetching market overview");
            
            RealTimeMetricsService.TradingMetrics metrics = metricsService.getRealTimeMetrics();
            
            MarketOverview overview = MarketOverview.builder()
                .totalVolume24h(metrics.getTotalVolumeUSDT())
                .totalTrades24h(metrics.getTotalTrades())
                .activeUsers24h(metrics.getActiveUsers())
                .averageOrderSize(calculateAverageOrderSize())
                .topGainers(getTopMovers("gainers", 5))
                .topLosers(getTopMovers("losers", 5))
                .marketSentiment(calculateMarketSentiment())
                .exchangeStatus("OPERATIONAL")
                .lastUpdated(Instant.now())
                .build();
            
            return ResponseEntity.ok(overview);
            
        } catch (Exception e) {
            logger.error("Error fetching market overview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/trading-volume")
    public ResponseEntity<List<VolumeData>> getTradingVolume(
            @RequestParam(defaultValue = "1h") String timeframe,
            @RequestParam(required = false) String symbol) {
        
        try {
            logger.info("Fetching trading volume for timeframe: {}, symbol: {}", timeframe, symbol);
            
            List<VolumeData> volumeData = getTradingVolumeData(timeframe, symbol);
            return ResponseEntity.ok(volumeData);
            
        } catch (Exception e) {
            logger.error("Error fetching trading volume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/price-movements")
    public ResponseEntity<List<PriceMovement>> getPriceMovements(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            logger.info("Fetching top {} price movements", limit);
            
            List<PriceMovement> movements = getTopPriceMovements(limit);
            return ResponseEntity.ok(movements);
            
        } catch (Exception e) {
            logger.error("Error fetching price movements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/symbol/{symbol}/metrics")
    public ResponseEntity<SymbolDashboard> getSymbolMetrics(@PathVariable String symbol) {
        try {
            logger.info("Fetching metrics for symbol: {}", symbol);
            
            RealTimeMetricsService.SymbolMetrics symbolMetrics = metricsService.getSymbolMetrics(symbol);
            OrderBookSnapshotService.OrderBookStats orderBookStats = orderBookService.getOrderBookStats(symbol);
            
            SymbolDashboard dashboard = SymbolDashboard.builder()
                .symbol(symbol)
                .currentPrice(symbolMetrics.getCurrentPrice())
                .highPrice24h(symbolMetrics.getHighPrice())
                .lowPrice24h(symbolMetrics.getLowPrice())
                .volume24h(symbolMetrics.getVolumeUSDT())
                .tradesCount24h(symbolMetrics.getTradesCount())
                .priceChange24h(calculatePriceChange24h(symbol))
                .priceChangePercent24h(calculatePriceChangePercent24h(symbol))
                .bestBid(orderBookStats != null ? orderBookStats.getBestBidPrice() : BigDecimal.ZERO)
                .bestAsk(orderBookStats != null ? orderBookStats.getBestAskPrice() : BigDecimal.ZERO)
                .spread(orderBookStats != null ? orderBookStats.getSpread() : BigDecimal.ZERO)
                .spreadPercent(orderBookStats != null ? orderBookStats.getSpreadPercentage() : BigDecimal.ZERO)
                .lastUpdated(Instant.now())
                .build();
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.error("Error fetching symbol metrics for: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/exchange-health")
    public ResponseEntity<ExchangeHealth> getExchangeHealth() {
        try {
            logger.info("Fetching exchange health metrics");
            
            ExchangeHealth health = ExchangeHealth.builder()
                .status("OPERATIONAL")
                .uptime(calculateUptime())
                .systemLoad(getSystemLoad())
                .memoryUsage(getMemoryUsage())
                .apiResponseTime(getApiResponseTime())
                .matchingEngineLatency(getMatchingEngineLatency())
                .orderProcessingRate(getOrderProcessingRate())
                .tradeProcessingRate(getTradeProcessingRate())
                .activeConnections(getActiveConnections())
                .lastHealthCheck(Instant.now())
                .build();
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error fetching exchange health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getRecentAlerts(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(required = false) String severity) {
        
        try {
            logger.info("Fetching alerts for last {} hours with severity: {}", hours, severity);
            
            List<Alert> alerts = getRecentAlertsData(Duration.ofHours(hours), severity);
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            logger.error("Error fetching recent alerts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/candlesticks/{symbol}")
    public ResponseEntity<List<Candlestick>> getCandlesticks(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "100") int limit) {
        
        try {
            logger.info("Fetching candlesticks for symbol: {}, interval: {}, limit: {}", 
                       symbol, interval, limit);
            
            List<Candlestick> candlesticks = getCandlestickData(symbol, interval, limit);
            return ResponseEntity.ok(candlesticks);
            
        } catch (Exception e) {
            logger.error("Error fetching candlesticks for symbol: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<OrderBookDashboard> getOrderBookDashboard(@PathVariable String symbol) {
        try {
            logger.info("Fetching order book dashboard for symbol: {}", symbol);
            
            OrderBookSnapshotService.OrderBookStats stats = orderBookService.getOrderBookStats(symbol);
            
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            
            OrderBookDashboard dashboard = OrderBookDashboard.builder()
                .symbol(symbol)
                .bestBidPrice(stats.getBestBidPrice())
                .bestAskPrice(stats.getBestAskPrice())
                .spread(stats.getSpread())
                .spreadPercentage(stats.getSpreadPercentage())
                .totalBidVolume(stats.getTotalBidVolume())
                .totalAskVolume(stats.getTotalAskVolume())
                .bidCount(stats.getBidCount())
                .askCount(stats.getAskCount())
                .imbalanceRatio(calculateImbalanceRatio(stats))
                .lastUpdated(stats.getTimestamp())
                .build();
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.error("Error fetching order book dashboard for symbol: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/risk-metrics")
    public ResponseEntity<RiskDashboard> getRiskMetrics() {
        try {
            logger.info("Fetching risk metrics dashboard");
            
            RiskDashboard riskDashboard = RiskDashboard.builder()
                .totalActiveAlerts(getTotalActiveAlerts())
                .highRiskUsers(getHighRiskUsersCount())
                .suspiciousTradesCount(getSuspiciousTradesCount())
                .fraudScore(calculateFraudScore())
                .riskLevel(calculateOverallRiskLevel())
                .topRiskFactors(getTopRiskFactors())
                .lastUpdated(Instant.now())
                .build();
            
            return ResponseEntity.ok(riskDashboard);
            
        } catch (Exception e) {
            logger.error("Error fetching risk metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper methods
    private BigDecimal calculateAverageOrderSize() {
        try {
            String today = LocalDate.now().toString();
            String key = "metrics:average_order_size:" + today;
            Double avgSize = (Double) redisTemplate.opsForValue().get(key);
            return avgSize != null ? BigDecimal.valueOf(avgSize) : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error calculating average order size", e);
            return BigDecimal.ZERO;
        }
    }
    
    private List<PriceMovement> getTopMovers(String type, int limit) {
        try {
            String key = "market:top_" + type;
            List<Object> data = redisTemplate.opsForList().range(key, 0, limit - 1);
            
            return data != null ? data.stream()
                .map(item -> (PriceMovement) item)
                .collect(java.util.stream.Collectors.toList()) 
                : java.util.Collections.emptyList();
                
        } catch (Exception e) {
            logger.error("Error getting top movers: {}", type, e);
            return java.util.Collections.emptyList();
        }
    }
    
    private String calculateMarketSentiment() {
        try {
            // Simplified sentiment calculation
            List<PriceMovement> gainers = getTopMovers("gainers", 10);
            List<PriceMovement> losers = getTopMovers("losers", 10);
            
            if (gainers.size() > losers.size()) {
                return "BULLISH";
            } else if (losers.size() > gainers.size()) {
                return "BEARISH";
            } else {
                return "NEUTRAL";
            }
        } catch (Exception e) {
            logger.error("Error calculating market sentiment", e);
            return "NEUTRAL";
        }
    }
    
    private List<VolumeData> getTradingVolumeData(String timeframe, String symbol) {
        // Simplified implementation - in production, query from InfluxDB
        return java.util.Collections.emptyList();
    }
    
    private List<PriceMovement> getTopPriceMovements(int limit) {
        List<PriceMovement> gainers = getTopMovers("gainers", limit / 2);
        List<PriceMovement> losers = getTopMovers("losers", limit / 2);
        
        List<PriceMovement> combined = new java.util.ArrayList<>();
        combined.addAll(gainers);
        combined.addAll(losers);
        
        return combined;
    }
    
    private BigDecimal calculatePriceChange24h(String symbol) {
        // Simplified implementation
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculatePriceChangePercent24h(String symbol) {
        // Simplified implementation
        return BigDecimal.ZERO;
    }
    
    private Duration calculateUptime() {
        // Simplified implementation
        return Duration.ofHours(72);
    }
    
    private double getSystemLoad() {
        return 0.45; // 45% load
    }
    
    private double getMemoryUsage() {
        return 0.67; // 67% memory usage
    }
    
    private long getApiResponseTime() {
        return 125; // 125ms average response time
    }
    
    private long getMatchingEngineLatency() {
        return 5; // 5ms matching engine latency
    }
    
    private long getOrderProcessingRate() {
        return 1500; // 1500 orders per second
    }
    
    private long getTradeProcessingRate() {
        return 800; // 800 trades per second
    }
    
    private long getActiveConnections() {
        return 25000; // 25k active connections
    }
    
    private List<Alert> getRecentAlertsData(Duration duration, String severity) {
        // Simplified implementation
        return java.util.Collections.emptyList();
    }
    
    private List<Candlestick> getCandlestickData(String symbol, String interval, int limit) {
        // Simplified implementation
        return java.util.Collections.emptyList();
    }
    
    private BigDecimal calculateImbalanceRatio(OrderBookSnapshotService.OrderBookStats stats) {
        if (stats.getTotalAskVolume().compareTo(BigDecimal.ZERO) > 0) {
            return stats.getTotalBidVolume().divide(stats.getTotalAskVolume(), 4, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private long getTotalActiveAlerts() {
        return 15; // Simplified
    }
    
    private long getHighRiskUsersCount() {
        return 3; // Simplified
    }
    
    private long getSuspiciousTradesCount() {
        return 7; // Simplified
    }
    
    private double calculateFraudScore() {
        return 0.15; // 15% fraud score
    }
    
    private String calculateOverallRiskLevel() {
        return "MEDIUM";
    }
    
    private List<String> getTopRiskFactors() {
        return List.of("WASH_TRADING", "HIGH_VELOCITY", "LARGE_ORDERS");
    }
}
