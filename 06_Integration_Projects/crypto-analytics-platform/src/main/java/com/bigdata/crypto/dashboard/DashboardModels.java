package com.bigdata.crypto.dashboard;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class DashboardModels {
}

class MarketOverview {
    private BigDecimal totalVolume24h;
    private Long totalTrades24h;
    private Long activeUsers24h;
    private BigDecimal averageOrderSize;
    private List<PriceMovement> topGainers;
    private List<PriceMovement> topLosers;
    private String marketSentiment;
    private String exchangeStatus;
    private Instant lastUpdated;
    
    public static MarketOverviewBuilder builder() {
        return new MarketOverviewBuilder();
    }
    
    public static class MarketOverviewBuilder {
        private MarketOverview overview = new MarketOverview();
        
        public MarketOverviewBuilder totalVolume24h(BigDecimal totalVolume24h) {
            overview.totalVolume24h = totalVolume24h;
            return this;
        }
        
        public MarketOverviewBuilder totalTrades24h(Long totalTrades24h) {
            overview.totalTrades24h = totalTrades24h;
            return this;
        }
        
        public MarketOverviewBuilder activeUsers24h(Long activeUsers24h) {
            overview.activeUsers24h = activeUsers24h;
            return this;
        }
        
        public MarketOverviewBuilder averageOrderSize(BigDecimal averageOrderSize) {
            overview.averageOrderSize = averageOrderSize;
            return this;
        }
        
        public MarketOverviewBuilder topGainers(List<PriceMovement> topGainers) {
            overview.topGainers = topGainers;
            return this;
        }
        
        public MarketOverviewBuilder topLosers(List<PriceMovement> topLosers) {
            overview.topLosers = topLosers;
            return this;
        }
        
        public MarketOverviewBuilder marketSentiment(String marketSentiment) {
            overview.marketSentiment = marketSentiment;
            return this;
        }
        
        public MarketOverviewBuilder exchangeStatus(String exchangeStatus) {
            overview.exchangeStatus = exchangeStatus;
            return this;
        }
        
        public MarketOverviewBuilder lastUpdated(Instant lastUpdated) {
            overview.lastUpdated = lastUpdated;
            return this;
        }
        
        public MarketOverview build() {
            return overview;
        }
    }
    
    // Getters
    public BigDecimal getTotalVolume24h() { return totalVolume24h; }
    public Long getTotalTrades24h() { return totalTrades24h; }
    public Long getActiveUsers24h() { return activeUsers24h; }
    public BigDecimal getAverageOrderSize() { return averageOrderSize; }
    public List<PriceMovement> getTopGainers() { return topGainers; }
    public List<PriceMovement> getTopLosers() { return topLosers; }
    public String getMarketSentiment() { return marketSentiment; }
    public String getExchangeStatus() { return exchangeStatus; }
    public Instant getLastUpdated() { return lastUpdated; }
}

class PriceMovement {
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private BigDecimal volume24h;
    private String direction; // UP, DOWN, STABLE
    private Instant timestamp;
    
    public PriceMovement() {}
    
    public PriceMovement(String symbol, BigDecimal currentPrice, BigDecimal priceChangePercent) {
        this.symbol = symbol;
        this.currentPrice = currentPrice;
        this.priceChangePercent = priceChangePercent;
        this.direction = priceChangePercent.compareTo(BigDecimal.ZERO) > 0 ? "UP" : 
                        priceChangePercent.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "STABLE";
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    
    public BigDecimal getPriceChange() { return priceChange; }
    public void setPriceChange(BigDecimal priceChange) { this.priceChange = priceChange; }
    
    public BigDecimal getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(BigDecimal priceChangePercent) { this.priceChangePercent = priceChangePercent; }
    
    public BigDecimal getVolume24h() { return volume24h; }
    public void setVolume24h(BigDecimal volume24h) { this.volume24h = volume24h; }
    
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

class VolumeData {
    private String symbol;
    private Instant timestamp;
    private BigDecimal volume;
    private Long tradeCount;
    private String timeframe;
    
    public VolumeData() {}
    
    public VolumeData(String symbol, Instant timestamp, BigDecimal volume, Long tradeCount) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.volume = volume;
        this.tradeCount = tradeCount;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    
    public Long getTradeCount() { return tradeCount; }
    public void setTradeCount(Long tradeCount) { this.tradeCount = tradeCount; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
}

class SymbolDashboard {
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal highPrice24h;
    private BigDecimal lowPrice24h;
    private BigDecimal volume24h;
    private Long tradesCount24h;
    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercent24h;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spread;
    private BigDecimal spreadPercent;
    private Instant lastUpdated;
    
    public static SymbolDashboardBuilder builder() {
        return new SymbolDashboardBuilder();
    }
    
    public static class SymbolDashboardBuilder {
        private SymbolDashboard dashboard = new SymbolDashboard();
        
        public SymbolDashboardBuilder symbol(String symbol) {
            dashboard.symbol = symbol;
            return this;
        }
        
        public SymbolDashboardBuilder currentPrice(BigDecimal currentPrice) {
            dashboard.currentPrice = currentPrice;
            return this;
        }
        
        public SymbolDashboardBuilder highPrice24h(BigDecimal highPrice24h) {
            dashboard.highPrice24h = highPrice24h;
            return this;
        }
        
        public SymbolDashboardBuilder lowPrice24h(BigDecimal lowPrice24h) {
            dashboard.lowPrice24h = lowPrice24h;
            return this;
        }
        
        public SymbolDashboardBuilder volume24h(BigDecimal volume24h) {
            dashboard.volume24h = volume24h;
            return this;
        }
        
        public SymbolDashboardBuilder tradesCount24h(Long tradesCount24h) {
            dashboard.tradesCount24h = tradesCount24h;
            return this;
        }
        
        public SymbolDashboardBuilder priceChange24h(BigDecimal priceChange24h) {
            dashboard.priceChange24h = priceChange24h;
            return this;
        }
        
        public SymbolDashboardBuilder priceChangePercent24h(BigDecimal priceChangePercent24h) {
            dashboard.priceChangePercent24h = priceChangePercent24h;
            return this;
        }
        
        public SymbolDashboardBuilder bestBid(BigDecimal bestBid) {
            dashboard.bestBid = bestBid;
            return this;
        }
        
        public SymbolDashboardBuilder bestAsk(BigDecimal bestAsk) {
            dashboard.bestAsk = bestAsk;
            return this;
        }
        
        public SymbolDashboardBuilder spread(BigDecimal spread) {
            dashboard.spread = spread;
            return this;
        }
        
        public SymbolDashboardBuilder spreadPercent(BigDecimal spreadPercent) {
            dashboard.spreadPercent = spreadPercent;
            return this;
        }
        
        public SymbolDashboardBuilder lastUpdated(Instant lastUpdated) {
            dashboard.lastUpdated = lastUpdated;
            return this;
        }
        
        public SymbolDashboard build() {
            return dashboard;
        }
    }
    
    // Getters
    public String getSymbol() { return symbol; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getHighPrice24h() { return highPrice24h; }
    public BigDecimal getLowPrice24h() { return lowPrice24h; }
    public BigDecimal getVolume24h() { return volume24h; }
    public Long getTradesCount24h() { return tradesCount24h; }
    public BigDecimal getPriceChange24h() { return priceChange24h; }
    public BigDecimal getPriceChangePercent24h() { return priceChangePercent24h; }
    public BigDecimal getBestBid() { return bestBid; }
    public BigDecimal getBestAsk() { return bestAsk; }
    public BigDecimal getSpread() { return spread; }
    public BigDecimal getSpreadPercent() { return spreadPercent; }
    public Instant getLastUpdated() { return lastUpdated; }
}

class ExchangeHealth {
    private String status;
    private Duration uptime;
    private double systemLoad;
    private double memoryUsage;
    private long apiResponseTime;
    private long matchingEngineLatency;
    private long orderProcessingRate;
    private long tradeProcessingRate;
    private long activeConnections;
    private Instant lastHealthCheck;
    
    public static ExchangeHealthBuilder builder() {
        return new ExchangeHealthBuilder();
    }
    
    public static class ExchangeHealthBuilder {
        private ExchangeHealth health = new ExchangeHealth();
        
        public ExchangeHealthBuilder status(String status) {
            health.status = status;
            return this;
        }
        
        public ExchangeHealthBuilder uptime(Duration uptime) {
            health.uptime = uptime;
            return this;
        }
        
        public ExchangeHealthBuilder systemLoad(double systemLoad) {
            health.systemLoad = systemLoad;
            return this;
        }
        
        public ExchangeHealthBuilder memoryUsage(double memoryUsage) {
            health.memoryUsage = memoryUsage;
            return this;
        }
        
        public ExchangeHealthBuilder apiResponseTime(long apiResponseTime) {
            health.apiResponseTime = apiResponseTime;
            return this;
        }
        
        public ExchangeHealthBuilder matchingEngineLatency(long matchingEngineLatency) {
            health.matchingEngineLatency = matchingEngineLatency;
            return this;
        }
        
        public ExchangeHealthBuilder orderProcessingRate(long orderProcessingRate) {
            health.orderProcessingRate = orderProcessingRate;
            return this;
        }
        
        public ExchangeHealthBuilder tradeProcessingRate(long tradeProcessingRate) {
            health.tradeProcessingRate = tradeProcessingRate;
            return this;
        }
        
        public ExchangeHealthBuilder activeConnections(long activeConnections) {
            health.activeConnections = activeConnections;
            return this;
        }
        
        public ExchangeHealthBuilder lastHealthCheck(Instant lastHealthCheck) {
            health.lastHealthCheck = lastHealthCheck;
            return this;
        }
        
        public ExchangeHealth build() {
            return health;
        }
    }
    
    // Getters
    public String getStatus() { return status; }
    public Duration getUptime() { return uptime; }
    public double getSystemLoad() { return systemLoad; }
    public double getMemoryUsage() { return memoryUsage; }
    public long getApiResponseTime() { return apiResponseTime; }
    public long getMatchingEngineLatency() { return matchingEngineLatency; }
    public long getOrderProcessingRate() { return orderProcessingRate; }
    public long getTradeProcessingRate() { return tradeProcessingRate; }
    public long getActiveConnections() { return activeConnections; }
    public Instant getLastHealthCheck() { return lastHealthCheck; }
}

class Alert {
    private String alertId;
    private String type;
    private String severity;
    private String message;
    private String source;
    private Instant timestamp;
    private String status;
    private String assignedTo;
    
    public Alert() {}
    
    public Alert(String type, String severity, String message) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.timestamp = Instant.now();
        this.status = "OPEN";
    }
    
    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}

class Candlestick {
    private String symbol;
    private Instant timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private Long tradeCount;
    private String interval;
    
    public Candlestick() {}
    
    public Candlestick(String symbol, Instant timestamp, BigDecimal open, BigDecimal high, 
                      BigDecimal low, BigDecimal close, BigDecimal volume) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }
    
    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }
    
    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }
    
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    
    public Long getTradeCount() { return tradeCount; }
    public void setTradeCount(Long tradeCount) { this.tradeCount = tradeCount; }
    
    public String getInterval() { return interval; }
    public void setInterval(String interval) { this.interval = interval; }
}

class OrderBookDashboard {
    private String symbol;
    private BigDecimal bestBidPrice;
    private BigDecimal bestAskPrice;
    private BigDecimal spread;
    private BigDecimal spreadPercentage;
    private BigDecimal totalBidVolume;
    private BigDecimal totalAskVolume;
    private int bidCount;
    private int askCount;
    private BigDecimal imbalanceRatio;
    private Instant lastUpdated;
    
    public static OrderBookDashboardBuilder builder() {
        return new OrderBookDashboardBuilder();
    }
    
    public static class OrderBookDashboardBuilder {
        private OrderBookDashboard dashboard = new OrderBookDashboard();
        
        public OrderBookDashboardBuilder symbol(String symbol) {
            dashboard.symbol = symbol;
            return this;
        }
        
        public OrderBookDashboardBuilder bestBidPrice(BigDecimal bestBidPrice) {
            dashboard.bestBidPrice = bestBidPrice;
            return this;
        }
        
        public OrderBookDashboardBuilder bestAskPrice(BigDecimal bestAskPrice) {
            dashboard.bestAskPrice = bestAskPrice;
            return this;
        }
        
        public OrderBookDashboardBuilder spread(BigDecimal spread) {
            dashboard.spread = spread;
            return this;
        }
        
        public OrderBookDashboardBuilder spreadPercentage(BigDecimal spreadPercentage) {
            dashboard.spreadPercentage = spreadPercentage;
            return this;
        }
        
        public OrderBookDashboardBuilder totalBidVolume(BigDecimal totalBidVolume) {
            dashboard.totalBidVolume = totalBidVolume;
            return this;
        }
        
        public OrderBookDashboardBuilder totalAskVolume(BigDecimal totalAskVolume) {
            dashboard.totalAskVolume = totalAskVolume;
            return this;
        }
        
        public OrderBookDashboardBuilder bidCount(int bidCount) {
            dashboard.bidCount = bidCount;
            return this;
        }
        
        public OrderBookDashboardBuilder askCount(int askCount) {
            dashboard.askCount = askCount;
            return this;
        }
        
        public OrderBookDashboardBuilder imbalanceRatio(BigDecimal imbalanceRatio) {
            dashboard.imbalanceRatio = imbalanceRatio;
            return this;
        }
        
        public OrderBookDashboardBuilder lastUpdated(Instant lastUpdated) {
            dashboard.lastUpdated = lastUpdated;
            return this;
        }
        
        public OrderBookDashboard build() {
            return dashboard;
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
    public BigDecimal getImbalanceRatio() { return imbalanceRatio; }
    public Instant getLastUpdated() { return lastUpdated; }
}

class RiskDashboard {
    private long totalActiveAlerts;
    private long highRiskUsers;
    private long suspiciousTradesCount;
    private double fraudScore;
    private String riskLevel;
    private List<String> topRiskFactors;
    private Instant lastUpdated;
    
    public static RiskDashboardBuilder builder() {
        return new RiskDashboardBuilder();
    }
    
    public static class RiskDashboardBuilder {
        private RiskDashboard dashboard = new RiskDashboard();
        
        public RiskDashboardBuilder totalActiveAlerts(long totalActiveAlerts) {
            dashboard.totalActiveAlerts = totalActiveAlerts;
            return this;
        }
        
        public RiskDashboardBuilder highRiskUsers(long highRiskUsers) {
            dashboard.highRiskUsers = highRiskUsers;
            return this;
        }
        
        public RiskDashboardBuilder suspiciousTradesCount(long suspiciousTradesCount) {
            dashboard.suspiciousTradesCount = suspiciousTradesCount;
            return this;
        }
        
        public RiskDashboardBuilder fraudScore(double fraudScore) {
            dashboard.fraudScore = fraudScore;
            return this;
        }
        
        public RiskDashboardBuilder riskLevel(String riskLevel) {
            dashboard.riskLevel = riskLevel;
            return this;
        }
        
        public RiskDashboardBuilder topRiskFactors(List<String> topRiskFactors) {
            dashboard.topRiskFactors = topRiskFactors;
            return this;
        }
        
        public RiskDashboardBuilder lastUpdated(Instant lastUpdated) {
            dashboard.lastUpdated = lastUpdated;
            return this;
        }
        
        public RiskDashboard build() {
            return dashboard;
        }
    }
    
    // Getters
    public long getTotalActiveAlerts() { return totalActiveAlerts; }
    public long getHighRiskUsers() { return highRiskUsers; }
    public long getSuspiciousTradesCount() { return suspiciousTradesCount; }
    public double getFraudScore() { return fraudScore; }
    public String getRiskLevel() { return riskLevel; }
    public List<String> getTopRiskFactors() { return topRiskFactors; }
    public Instant getLastUpdated() { return lastUpdated; }
}
