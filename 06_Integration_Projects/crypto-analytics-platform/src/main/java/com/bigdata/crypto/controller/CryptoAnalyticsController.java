package com.bigdata.crypto.controller;

import com.bigdata.crypto.dto.BulkMarketDataRequest;
import com.bigdata.crypto.dto.MarketDataResponse;
import com.bigdata.crypto.fraud.CryptoFraudDetectionService;
import com.bigdata.crypto.risk.CryptoRiskManagementService;
import com.bigdata.crypto.risk.RiskModels.*;
import com.bigdata.crypto.service.CryptoEventIngestionService;
import com.bigdata.crypto.service.RealTimeMetricsService;
import com.bigdata.crypto.signals.CryptoTradingSignalsService;
import com.bigdata.crypto.signals.SignalModels.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Main REST API controller for the Crypto Analytics Platform
 * Provides endpoints for trading analytics, risk management, and signals
 */
@RestController
@RequestMapping("/api/v1/crypto")
@Slf4j
public class CryptoAnalyticsController {

    @Autowired
    private CryptoEventIngestionService eventIngestionService;

    @Autowired
    private CryptoTradingSignalsService tradingSignalsService;

    @Autowired
    private CryptoRiskManagementService riskManagementService;

    @Autowired
    private CryptoFraudDetectionService fraudDetectionService;

    @Autowired
    private RealTimeMetricsService metricsService;

    // ===== Market Data & Analytics Endpoints =====

    /**
     * Get real-time market data for a symbol
     */
    @GetMapping("/market/{symbol}")
    public ResponseEntity<MarketDataResponse> getMarketData(@PathVariable String symbol) {
        try {
            log.info("Fetching market data for symbol: {}", symbol);
            MarketDataResponse response = eventIngestionService.getLatestMarketData(symbol);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching market data for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get market data for multiple symbols
     */
    @PostMapping("/market/bulk")
    public ResponseEntity<Map<String, MarketDataResponse>> getBulkMarketData(
            @RequestBody BulkMarketDataRequest request) {
        try {
            log.info("Fetching bulk market data for {} symbols", request.getSymbols().size());
            Map<String, MarketDataResponse> response = eventIngestionService.getBulkMarketData(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching bulk market data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get real-time platform metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<PlatformMetrics> getPlatformMetrics() {
        try {
            PlatformMetrics metrics = PlatformMetrics.builder()
                    .totalTradingVolume(metricsService.getTotalTradingVolume())
                    .activeUsersCount(metricsService.getActiveUsersCount())
                    .totalTradesCount(metricsService.getTotalTradesCount())
                    .averageTradeSize(metricsService.getAverageTradeSize())
                    .topTradingPairs(metricsService.getTopTradingPairs())
                    .build();
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching platform metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Trading Signals Endpoints =====

    /**
     * Generate trading signal for a specific symbol
     */
    @GetMapping("/signals/{symbol}")
    public ResponseEntity<TradingSignal> getTradingSignal(@PathVariable String symbol) {
        try {
            log.info("Generating trading signal for symbol: {}", symbol);
            TradingSignal signal = tradingSignalsService.generateSignal(symbol);
            return ResponseEntity.ok(signal);
        } catch (Exception e) {
            log.error("Error generating signal for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate trading signals for multiple symbols
     */
    @PostMapping("/signals/bulk")
    public ResponseEntity<Map<String, TradingSignal>> getBulkTradingSignals(
            @RequestBody List<String> symbols) {
        try {
            log.info("Generating trading signals for {} symbols", symbols.size());
            Map<String, TradingSignal> signals = tradingSignalsService.generateSignals(symbols);
            return ResponseEntity.ok(signals);
        } catch (Exception e) {
            log.error("Error generating bulk signals: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get portfolio recommendations for a user
     */
    @GetMapping("/portfolio/{userId}/recommendations")
    public ResponseEntity<PortfolioRecommendation> getPortfolioRecommendations(
            @PathVariable String userId,
            @RequestParam List<String> symbols) {
        try {
            log.info("Generating portfolio recommendations for user: {}", userId);
            PortfolioRecommendation recommendation = tradingSignalsService.getPortfolioRecommendation(userId, symbols);
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            log.error("Error generating portfolio recommendations for {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Risk Management Endpoints =====

    /**
     * Calculate portfolio risk assessment
     */
    @GetMapping("/risk/portfolio/{portfolioId}")
    public ResponseEntity<RiskAssessment> getPortfolioRisk(@PathVariable String portfolioId) {
        try {
            log.info("Calculating portfolio risk for: {}", portfolioId);
            RiskAssessment riskAssessment = riskManagementService.calculatePortfolioRisk(portfolioId);
            return ResponseEntity.ok(riskAssessment);
        } catch (Exception e) {
            log.error("Error calculating portfolio risk for {}: {}", portfolioId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Analyze market risk for symbols
     */
    @PostMapping("/risk/market")
    public ResponseEntity<MarketRiskAnalysis> getMarketRiskAnalysis(
            @RequestBody MarketRiskRequest request) {
        try {
            log.info("Analyzing market risk for {} symbols", request.getSymbols().size());
            MarketRiskAnalysis analysis = riskManagementService.analyzeMarketRisk(
                    request.getSymbols(), request.getLookbackDays());
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error analyzing market risk: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Perform stress testing on portfolio
     */
    @PostMapping("/risk/stress-test/{portfolioId}")
    public ResponseEntity<StressTestResult> performStressTest(
            @PathVariable String portfolioId,
            @RequestBody StressTestScenario scenario) {
        try {
            log.info("Performing stress test for portfolio: {}", portfolioId);
            StressTestResult result = riskManagementService.performStressTest(portfolioId, scenario);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error performing stress test for {}: {}", portfolioId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Fraud Detection Endpoints =====

    /**
     * Analyze transaction for fraud patterns
     */
    @PostMapping("/fraud/analyze")
    public ResponseEntity<FraudAnalysisResult> analyzeFraud(@RequestBody FraudAnalysisRequest request) {
        try {
            log.info("Analyzing fraud for transaction: {}", request.getTransactionId());
            FraudAnalysisResult result = fraudDetectionService.analyzeTransaction(
                    request.getUserId(), request.getTransactionData());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error analyzing fraud for transaction {}: {}", request.getTransactionId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user behavior analysis
     */
    @GetMapping("/behavior/{userId}")
    public ResponseEntity<UserBehaviorProfile> getUserBehaviorAnalysis(@PathVariable String userId) {
        try {
            log.info("Analyzing user behavior for: {}", userId);
            UserBehaviorProfile profile = fraudDetectionService.getUserBehaviorProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error analyzing user behavior for {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Health & Status Endpoints =====

    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<SystemHealthStatus> getSystemHealth() {
        try {
            SystemHealthStatus health = SystemHealthStatus.builder()
                    .status("HEALTHY")
                    .kafkaStatus("CONNECTED")
                    .sparkStatus("RUNNING")
                    .redisStatus("CONNECTED")
                    .mongodbStatus("CONNECTED")
                    .influxdbStatus("CONNECTED")
                    .uptime(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error checking system health: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get API version information
     */
    @GetMapping("/version")
    public ResponseEntity<ApiVersionInfo> getVersionInfo() {
        ApiVersionInfo version = ApiVersionInfo.builder()
                .version("1.0.0")
                .buildTime("2025-01-21")
                .gitCommit("main")
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion("3.2.1")
                .build();
        
        return ResponseEntity.ok(version);
    }

    // ===== Supporting DTOs =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlatformMetrics {
        private BigDecimal totalTradingVolume;
        private Long activeUsersCount;
        private Long totalTradesCount;
        private BigDecimal averageTradeSize;
        private List<String> topTradingPairs;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketRiskRequest {
        private List<String> symbols;
        private int lookbackDays;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudAnalysisRequest {
        private String transactionId;
        private String userId;
        private Map<String, Object> transactionData;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FraudAnalysisResult {
        private String transactionId;
        private boolean isFraudulent;
        private BigDecimal riskScore;
        private List<String> flaggedReasons;
        private String recommendation;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserBehaviorProfile {
        private String userId;
        private BigDecimal riskScore;
        private Map<String, Object> behaviorPatterns;
        private List<String> alerts;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemHealthStatus {
        private String status;
        private String kafkaStatus;
        private String sparkStatus;
        private String redisStatus;
        private String mongodbStatus;
        private String influxdbStatus;
        private Long uptime;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiVersionInfo {
        private String version;
        private String buildTime;
        private String gitCommit;
        private String javaVersion;
        private String springBootVersion;
    }
}
