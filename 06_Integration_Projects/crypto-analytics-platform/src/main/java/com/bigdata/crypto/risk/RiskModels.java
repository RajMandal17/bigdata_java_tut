package com.bigdata.crypto.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Risk assessment models and enums for cryptocurrency risk management
 */
public class RiskModels {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAssessment {
        private String portfolioId;
        private Instant timestamp;
        private BigDecimal var95;
        private BigDecimal var99;
        private BigDecimal totalExposure;
        private Map<String, BigDecimal> concentrationByAsset;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private RiskLevel riskScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionRisk {
        private String userId;
        private String symbol;
        private BigDecimal currentPosition;
        private BigDecimal averagePrice;
        private BigDecimal unrealizedPnL;
        private BigDecimal leverage;
        private BigDecimal marginUsed;
        private BigDecimal liquidationPrice;
        private RiskLevel riskLevel;
        private Instant timestamp;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PositionInfo {
        private String symbol;
        private String userId;
        private BigDecimal quantity;
        private BigDecimal averagePrice;
        private BigDecimal marketValue;
        private BigDecimal collateral;
        private Instant lastUpdateTime;
        
        public PositionInfo() {
            this.quantity = BigDecimal.ZERO;
            this.averagePrice = BigDecimal.ZERO;
            this.marketValue = BigDecimal.ZERO;
            this.collateral = BigDecimal.ZERO;
            this.lastUpdateTime = Instant.now();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketRiskAnalysis {
        private java.util.List<String> symbols;
        private Instant analysisDate;
        private int lookbackDays;
        private Map<String, BigDecimal> volatilities;
        private Map<String, Map<String, BigDecimal>> correlationMatrix;
        private Map<String, BigDecimal> betas;
        private MarketRegime marketRegime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestResult {
        private String portfolioId;
        private StressTestScenario scenario;
        private Instant testDate;
        private BigDecimal totalPnL;
        private Map<String, BigDecimal> pnlByAsset;
        private StressSeverity severity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StressTestScenario {
        private String name;
        private StressType type;
        private Map<String, BigDecimal> adjustments;
        private String description;
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum MarketRegime {
        BULL,
        BEAR,
        SIDEWAYS,
        VOLATILE,
        NORMAL
    }

    public enum StressType {
        MARKET_CRASH,
        VOLATILITY_SPIKE,
        LIQUIDITY_CRISIS,
        REGULATORY_SHOCK,
        TECHNICAL_FAILURE
    }

    public enum StressSeverity {
        MILD,
        MODERATE,
        SEVERE,
        CATASTROPHIC
    }
}
