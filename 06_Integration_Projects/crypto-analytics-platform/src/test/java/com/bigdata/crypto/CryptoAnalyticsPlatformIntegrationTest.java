package com.bigdata.crypto;

import com.bigdata.crypto.controller.CryptoAnalyticsController;
import com.bigdata.crypto.fraud.CryptoFraudDetectionService;
import com.bigdata.crypto.risk.CryptoRiskManagementService;
import com.bigdata.crypto.risk.RiskModels.*;
import com.bigdata.crypto.service.CryptoEventIngestionService;
import com.bigdata.crypto.signals.CryptoTradingSignalsService;
import com.bigdata.crypto.signals.SignalModels.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Crypto Analytics Platform
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class CryptoAnalyticsPlatformIntegrationTest {

    @Autowired
    private CryptoEventIngestionService eventIngestionService;

    @Autowired
    private CryptoTradingSignalsService tradingSignalsService;

    @Autowired
    private CryptoRiskManagementService riskManagementService;

    @Autowired
    private CryptoFraudDetectionService fraudDetectionService;

    @Autowired
    private CryptoAnalyticsController analyticsController;

    @Test
    public void testPlatformInitialization() {
        log.info("Testing platform initialization...");
        
        // Verify all services are properly injected
        assertNotNull(eventIngestionService, "Event ingestion service should be available");
        assertNotNull(tradingSignalsService, "Trading signals service should be available");
        assertNotNull(riskManagementService, "Risk management service should be available");
        assertNotNull(fraudDetectionService, "Fraud detection service should be available");
        assertNotNull(analyticsController, "Analytics controller should be available");
        
        log.info("✅ Platform initialization test passed");
    }

    @Test
    public void testTradingSignalsGeneration() {
        log.info("Testing trading signals generation...");
        
        try {
            // Test single symbol signal generation
            String testSymbol = "BTC-USD";
            TradingSignal signal = tradingSignalsService.generateSignal(testSymbol);
            
            assertNotNull(signal, "Signal should not be null");
            assertEquals(testSymbol, signal.getSymbol(), "Symbol should match");
            assertNotNull(signal.getSignalType(), "Signal type should not be null");
            assertNotNull(signal.getStrength(), "Signal strength should not be null");
            assertNotNull(signal.getConfidence(), "Confidence should not be null");
            assertNotNull(signal.getTimestamp(), "Timestamp should not be null");
            
            // Test multiple symbols
            List<String> symbols = Arrays.asList("BTC-USD", "ETH-USD", "ADA-USD");
            Map<String, TradingSignal> signals = tradingSignalsService.generateSignals(symbols);
            
            assertNotNull(signals, "Signals map should not be null");
            assertEquals(symbols.size(), signals.size(), "Should have signals for all symbols");
            
            // Test portfolio recommendation
            String userId = "test-user-1";
            PortfolioRecommendation recommendation = tradingSignalsService.getPortfolioRecommendation(userId, symbols);
            
            assertNotNull(recommendation, "Portfolio recommendation should not be null");
            assertEquals(userId, recommendation.getUserId(), "User ID should match");
            assertNotNull(recommendation.getRecommendations(), "Recommendations should not be null");
            assertNotNull(recommendation.getMarketSentiment(), "Market sentiment should not be null");
            
            log.info("✅ Trading signals generation test passed");
            
        } catch (Exception e) {
            log.error("❌ Trading signals test failed: {}", e.getMessage(), e);
            fail("Trading signals test failed: " + e.getMessage());
        }
    }

    @Test
    public void testRiskManagement() {
        log.info("Testing risk management functionality...");
        
        try {
            // Test portfolio risk calculation
            String portfolioId = "test-portfolio-1";
            RiskAssessment riskAssessment = riskManagementService.calculatePortfolioRisk(portfolioId);
            
            assertNotNull(riskAssessment, "Risk assessment should not be null");
            assertEquals(portfolioId, riskAssessment.getPortfolioId(), "Portfolio ID should match");
            assertNotNull(riskAssessment.getVar95(), "VaR 95% should not be null");
            assertNotNull(riskAssessment.getVar99(), "VaR 99% should not be null");
            assertNotNull(riskAssessment.getTotalExposure(), "Total exposure should not be null");
            assertNotNull(riskAssessment.getRiskScore(), "Risk score should not be null");
            
            // Test market risk analysis
            List<String> symbols = Arrays.asList("BTC-USD", "ETH-USD");
            int lookbackDays = 30;
            MarketRiskAnalysis marketRisk = riskManagementService.analyzeMarketRisk(symbols, lookbackDays);
            
            assertNotNull(marketRisk, "Market risk analysis should not be null");
            assertEquals(symbols, marketRisk.getSymbols(), "Symbols should match");
            assertEquals(lookbackDays, marketRisk.getLookbackDays(), "Lookback days should match");
            assertNotNull(marketRisk.getMarketRegime(), "Market regime should not be null");
            
            // Test stress testing
            StressTestScenario scenario = StressTestScenario.builder()
                    .name("Market Crash Test")
                    .type(StressType.MARKET_CRASH)
                    .description("Simulated market crash scenario")
                    .build();
            
            StressTestResult stressResult = riskManagementService.performStressTest(portfolioId, scenario);
            
            assertNotNull(stressResult, "Stress test result should not be null");
            assertEquals(portfolioId, stressResult.getPortfolioId(), "Portfolio ID should match");
            assertEquals(scenario, stressResult.getScenario(), "Scenario should match");
            assertNotNull(stressResult.getTotalPnL(), "Total PnL should not be null");
            assertNotNull(stressResult.getSeverity(), "Severity should not be null");
            
            log.info("✅ Risk management test passed");
            
        } catch (Exception e) {
            log.error("❌ Risk management test failed: {}", e.getMessage(), e);
            fail("Risk management test failed: " + e.getMessage());
        }
    }

    @Test
    public void testFraudDetection() {
        log.info("Testing fraud detection functionality...");
        
        try {
            String userId = "test-user-fraud";
            Map<String, Object> transactionData = Map.of(
                    "amount", new BigDecimal("10000"),
                    "symbol", "BTC-USD",
                    "timestamp", System.currentTimeMillis(),
                    "ipAddress", "192.168.1.1",
                    "deviceId", "device-123"
            );
            
            // Test transaction analysis
            var fraudResult = fraudDetectionService.analyzeTransaction(userId, transactionData);
            
            assertNotNull(fraudResult, "Fraud analysis result should not be null");
            assertNotNull(fraudResult.getRiskScore(), "Risk score should not be null");
            assertTrue(fraudResult.getRiskScore().compareTo(BigDecimal.ZERO) >= 0, "Risk score should be non-negative");
            assertTrue(fraudResult.getRiskScore().compareTo(new BigDecimal("100")) <= 0, "Risk score should not exceed 100");
            
            // Test user behavior analysis
            var behaviorProfile = fraudDetectionService.getUserBehaviorProfile(userId);
            
            assertNotNull(behaviorProfile, "Behavior profile should not be null");
            assertEquals(userId, behaviorProfile.getUserId(), "User ID should match");
            assertNotNull(behaviorProfile.getRiskScore(), "Risk score should not be null");
            assertNotNull(behaviorProfile.getBehaviorPatterns(), "Behavior patterns should not be null");
            
            log.info("✅ Fraud detection test passed");
            
        } catch (Exception e) {
            log.error("❌ Fraud detection test failed: {}", e.getMessage(), e);
            fail("Fraud detection test failed: " + e.getMessage());
        }
    }

    @Test
    public void testAnalyticsController() {
        log.info("Testing analytics controller endpoints...");
        
        try {
            // Test market data endpoint
            String testSymbol = "BTC-USD";
            var marketDataResponse = analyticsController.getMarketData(testSymbol);
            
            assertNotNull(marketDataResponse, "Market data response should not be null");
            assertEquals(200, marketDataResponse.getStatusCodeValue(), "Should return 200 OK");
            assertNotNull(marketDataResponse.getBody(), "Response body should not be null");
            
            // Test trading signals endpoint
            var signalResponse = analyticsController.getTradingSignal(testSymbol);
            
            assertNotNull(signalResponse, "Signal response should not be null");
            assertEquals(200, signalResponse.getStatusCodeValue(), "Should return 200 OK");
            assertNotNull(signalResponse.getBody(), "Response body should not be null");
            
            // Test platform metrics endpoint
            var metricsResponse = analyticsController.getPlatformMetrics();
            
            assertNotNull(metricsResponse, "Metrics response should not be null");
            assertEquals(200, metricsResponse.getStatusCodeValue(), "Should return 200 OK");
            assertNotNull(metricsResponse.getBody(), "Response body should not be null");
            
            // Test system health endpoint
            var healthResponse = analyticsController.getSystemHealth();
            
            assertNotNull(healthResponse, "Health response should not be null");
            assertEquals(200, healthResponse.getStatusCodeValue(), "Should return 200 OK");
            assertNotNull(healthResponse.getBody(), "Response body should not be null");
            assertEquals("HEALTHY", healthResponse.getBody().getStatus(), "System should be healthy");
            
            // Test version info endpoint
            var versionResponse = analyticsController.getVersionInfo();
            
            assertNotNull(versionResponse, "Version response should not be null");
            assertEquals(200, versionResponse.getStatusCodeValue(), "Should return 200 OK");
            assertNotNull(versionResponse.getBody(), "Response body should not be null");
            assertNotNull(versionResponse.getBody().getVersion(), "Version should not be null");
            
            log.info("✅ Analytics controller test passed");
            
        } catch (Exception e) {
            log.error("❌ Analytics controller test failed: {}", e.getMessage(), e);
            fail("Analytics controller test failed: " + e.getMessage());
        }
    }

    @Test
    public void testEndToEndWorkflow() {
        log.info("Testing end-to-end analytics workflow...");
        
        try {
            String userId = "test-user-e2e";
            String symbol = "BTC-USD";
            String portfolioId = "portfolio-e2e";
            
            // 1. Generate trading signal
            TradingSignal signal = tradingSignalsService.generateSignal(symbol);
            assertNotNull(signal, "Signal should be generated");
            log.info("✅ Step 1: Trading signal generated - {}", signal.getSignalType());
            
            // 2. Assess portfolio risk
            RiskAssessment riskAssessment = riskManagementService.calculatePortfolioRisk(portfolioId);
            assertNotNull(riskAssessment, "Risk assessment should be performed");
            log.info("✅ Step 2: Risk assessment completed - Risk Level: {}", riskAssessment.getRiskScore());
            
            // 3. Check for fraud patterns
            Map<String, Object> transactionData = Map.of(
                    "amount", new BigDecimal("5000"),
                    "symbol", symbol,
                    "signalType", signal.getSignalType().toString()
            );
            
            var fraudResult = fraudDetectionService.analyzeTransaction(userId, transactionData);
            assertNotNull(fraudResult, "Fraud analysis should be performed");
            log.info("✅ Step 3: Fraud analysis completed - Risk Score: {}", fraudResult.getRiskScore());
            
            // 4. Generate portfolio recommendation
            PortfolioRecommendation recommendation = tradingSignalsService.getPortfolioRecommendation(
                    userId, Arrays.asList(symbol));
            assertNotNull(recommendation, "Portfolio recommendation should be generated");
            log.info("✅ Step 4: Portfolio recommendation generated - {} recommendations", 
                    recommendation.getRecommendations().size());
            
            // 5. Validate the complete workflow
            assertTrue(signal.getConfidence().compareTo(BigDecimal.ZERO) >= 0, "Signal confidence should be valid");
            assertTrue(riskAssessment.getTotalExposure().compareTo(BigDecimal.ZERO) >= 0, "Exposure should be valid");
            assertTrue(fraudResult.getRiskScore().compareTo(BigDecimal.ZERO) >= 0, "Fraud score should be valid");
            assertNotNull(recommendation.getMarketSentiment(), "Market sentiment should be determined");
            
            log.info("✅ End-to-end workflow test passed");
            
        } catch (Exception e) {
            log.error("❌ End-to-end workflow test failed: {}", e.getMessage(), e);
            fail("End-to-end workflow test failed: " + e.getMessage());
        }
    }

    @Test
    public void testPerformanceAndScalability() {
        log.info("Testing performance and scalability...");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Test bulk signal generation
            List<String> symbols = Arrays.asList("BTC-USD", "ETH-USD", "ADA-USD", "DOT-USD", "LINK-USD");
            Map<String, TradingSignal> signals = tradingSignalsService.generateSignals(symbols);
            
            long signalTime = System.currentTimeMillis() - startTime;
            
            assertEquals(symbols.size(), signals.size(), "Should generate signals for all symbols");
            assertTrue(signalTime < 5000, "Bulk signal generation should complete within 5 seconds");
            
            log.info("✅ Bulk signal generation completed in {} ms", signalTime);
            
            // Test concurrent risk assessments
            startTime = System.currentTimeMillis();
            
            List<String> portfolios = Arrays.asList("portfolio-1", "portfolio-2", "portfolio-3");
            for (String portfolio : portfolios) {
                RiskAssessment risk = riskManagementService.calculatePortfolioRisk(portfolio);
                assertNotNull(risk, "Risk assessment should be completed for " + portfolio);
            }
            
            long riskTime = System.currentTimeMillis() - startTime;
            assertTrue(riskTime < 3000, "Risk assessments should complete within 3 seconds");
            
            log.info("✅ Risk assessments completed in {} ms", riskTime);
            
            log.info("✅ Performance and scalability test passed");
            
        } catch (Exception e) {
            log.error("❌ Performance test failed: {}", e.getMessage(), e);
            fail("Performance test failed: " + e.getMessage());
        }
    }
}
