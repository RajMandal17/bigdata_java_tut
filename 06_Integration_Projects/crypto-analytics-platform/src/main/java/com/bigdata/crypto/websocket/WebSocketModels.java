package com.bigdata.crypto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * WebSocket message models for real-time communication
 */
public class WebSocketModels {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSocketRequest {
        private String topic;
        private String symbol;
        private String userId;
        private String action;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketDataUpdate {
        private String symbol;
        private BigDecimal price;
        private BigDecimal volume;
        private BigDecimal change24h;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingSignalUpdate {
        private String symbol;
        private String signalType;
        private String strength;
        private BigDecimal confidence;
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;
        private String reasoning;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioMetricsUpdate {
        private String userId;
        private BigDecimal totalValue;
        private BigDecimal totalPnL;
        private BigDecimal dailyPnL;
        private BigDecimal riskScore;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookUpdate {
        private String symbol;
        private List<OrderBookEntry> bids;
        private List<OrderBookEntry> asks;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class OrderBookEntry {
        private BigDecimal price;
        private BigDecimal quantity;
        
        public OrderBookEntry(BigDecimal price, BigDecimal quantity) {
            this.price = price;
            this.quantity = quantity;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsUpdate {
        private BigDecimal totalTradingVolume;
        private Long activeTradersCount;
        private List<String> topPerformingAssets;
        private String marketSentiment;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorMessage {
        private String error;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeAlert {
        private String userId;
        private String symbol;
        private String alertType;
        private String message;
        private BigDecimal triggerPrice;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAlert {
        private String userId;
        private String riskType;
        private String severity;
        private String message;
        private BigDecimal riskScore;
        private Instant timestamp;
    }
}
