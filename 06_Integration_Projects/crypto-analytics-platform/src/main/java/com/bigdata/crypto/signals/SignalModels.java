package com.bigdata.crypto.signals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Trading signals and related model classes
 */
public class SignalModels {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradingSignal {
        private String symbol;
        private SignalType signalType;
        private SignalStrength strength;
        private BigDecimal confidence;
        private BigDecimal currentPrice;
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;
        private MarketCondition marketCondition;
        private TechnicalIndicators indicators;
        private Instant timestamp;
        private String reasoning;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalIndicators {
        private BigDecimal sma20;
        private BigDecimal sma50;
        private BigDecimal ema12;
        private BigDecimal ema26;
        private BigDecimal macdLine;
        private BigDecimal macdSignal;
        private BigDecimal macdHistogram;
        private BigDecimal rsi;
        private BigDecimal bollingerUpper;
        private BigDecimal bollingerMiddle;
        private BigDecimal bollingerLower;
        private BigDecimal stochasticK;
        private BigDecimal stochasticD;
        private BigDecimal volumeSMA;
        private BigDecimal volumeRatio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private Instant timestamp;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal volume;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacdResult {
        private BigDecimal macdLine;
        private BigDecimal signalLine;
        private BigDecimal histogram;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BollingerBands {
        private BigDecimal upperBand;
        private BigDecimal middleBand;
        private BigDecimal lowerBand;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StochasticResult {
        private BigDecimal percentK;
        private BigDecimal percentD;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioRecommendation {
        private String userId;
        private List<TradeRecommendation> recommendations;
        private BigDecimal portfolioRiskScore;
        private MarketSentiment marketSentiment;
        private BigDecimal diversificationScore;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRecommendation {
        private String symbol;
        private String action;
        private BigDecimal confidence;
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;
        private String reasoning;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioAnalysis {
        private BigDecimal diversificationScore;
        private Map<String, Map<String, BigDecimal>> correlationMatrix;
    }

    public enum SignalType {
        BUY,
        SELL,
        HOLD
    }

    public enum SignalStrength {
        WEAK,
        MODERATE,
        STRONG
    }

    public enum MarketCondition {
        BULLISH,
        BEARISH,
        SIDEWAYS,
        VOLATILE,
        UNKNOWN
    }

    public enum MarketSentiment {
        BULLISH,
        BEARISH,
        NEUTRAL
    }
}
