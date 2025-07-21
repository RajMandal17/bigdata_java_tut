package com.bigdata.crypto.fraud;

import com.bigdata.crypto.model.TradeEvent;
import com.bigdata.crypto.model.OrderEvent;
import com.bigdata.crypto.controller.CryptoAnalyticsController.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CryptoFraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoFraudDetectionService.class);
    
    @Autowired
    private CryptoMLModelService mlModelService;
    
    @Autowired
    private TradingRuleEngineService ruleEngine;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private UserBehaviorAnalysisService behaviorAnalysis;
    
    @KafkaListener(topics = "gitbitex-trades", groupId = "fraud-detection")
    public void detectTradingFraud(TradeEvent trade) {
        try {
            logger.debug("Analyzing trade for fraud: {}", trade.getTradeId());
            
            // Multi-layered fraud detection
            
            // 1. Rule-based detection (fast, real-time)
            RiskScore ruleBasedScore = ruleEngine.evaluateTrade(trade);
            
            // 2. ML-based anomaly detection
            AnomalyScore mlAnomalyScore = mlModelService.detectTradeAnomaly(trade);
            
            // 3. Trading pattern analysis
            TradingBehaviorScore behaviorScore = analyzeTradingBehavior(trade);
            
            // 4. Real-time velocity checks
            VelocityCheckResult velocityResult = performVelocityChecks(trade);
            
            // Combine all risk factors
            ComprehensiveRiskAssessment riskAssessment = combineRiskAssessments(
                ruleBasedScore, mlAnomalyScore, behaviorScore, velocityResult);
            
            // Take action based on risk level
            handleRiskAssessment(trade, riskAssessment);
            
        } catch (Exception e) {
            logger.error("Error in fraud detection for trade: " + trade.getTradeId(), e);
        }
    }
    
    @KafkaListener(topics = "gitbitex-orders", groupId = "fraud-detection")
    public void detectOrderFraud(OrderEvent order) {
        try {
            logger.debug("Analyzing order for fraud: {}", order.getOrderId());
            
            // Order-specific fraud checks
            OrderRiskAssessment orderRisk = analyzeOrderFraud(order);
            
            if (orderRisk.getRiskLevel() == RiskLevel.HIGH) {
                alertService.sendFraudAlert(FraudAlert.builder()
                    .type("SUSPICIOUS_ORDER")
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .symbol(order.getSymbol())
                    .riskFactors(orderRisk.getRiskFactors())
                    .riskScore(orderRisk.getRiskScore())
                    .recommendedAction("FREEZE_ORDER")
                    .timestamp(Instant.now())
                    .build());
            }
            
        } catch (Exception e) {
            logger.error("Error in order fraud detection: " + order.getOrderId(), e);
        }
    }
    
    /**
     * Analyze transaction for fraud patterns
     */
    public FraudAnalysisResult analyzeTransaction(String userId, Map<String, Object> transactionData) {
        try {
            logger.info("Analyzing transaction for user: {}", userId);
            
            // Calculate risk score based on transaction patterns
            BigDecimal riskScore = calculateTransactionRiskScore(transactionData);
            
            // Check for fraud indicators
            List<String> flaggedReasons = checkFraudIndicators(transactionData);
            
            // Determine if transaction is fraudulent
            boolean isFraudulent = riskScore.compareTo(new BigDecimal("75")) > 0;
            
            // Generate recommendation
            String recommendation = generateRecommendation(riskScore, isFraudulent);
            
            return FraudAnalysisResult.builder()
                    .transactionId(transactionData.get("transactionId") != null ? 
                            transactionData.get("transactionId").toString() : "unknown")
                    .isFraudulent(isFraudulent)
                    .riskScore(riskScore)
                    .flaggedReasons(flaggedReasons)
                    .recommendation(recommendation)
                    .build();
            
        } catch (Exception e) {
            logger.error("Error analyzing transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Transaction analysis failed", e);
        }
    }
    
    /**
     * Get user behavior profile
     */
    public UserBehaviorProfile getUserBehaviorProfile(String userId) {
        try {
            logger.info("Getting behavior profile for user: {}", userId);
            
            // Simplified behavior analysis
            BigDecimal riskScore = new BigDecimal("45"); // Medium risk
            
            Map<String, Object> behaviorPatterns = Map.of(
                    "averageTradeSize", new BigDecimal("1000"),
                    "tradingFrequency", "MODERATE",
                    "preferredAssets", List.of("BTC-USD", "ETH-USD"),
                    "riskTolerance", "MEDIUM"
            );
            
            List<String> alerts = List.of();
            
            return UserBehaviorProfile.builder()
                    .userId(userId)
                    .riskScore(riskScore)
                    .behaviorPatterns(behaviorPatterns)
                    .alerts(alerts)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error getting user behavior profile: {}", e.getMessage(), e);
            throw new RuntimeException("Behavior analysis failed", e);
        }
    }
    
    private TradingBehaviorScore analyzeTradingBehavior(TradeEvent trade) {
        try {
            // Analyze historical trading patterns
            List<TradeEvent> recentTrades = behaviorAnalysis.getRecentTrades(trade.getUserId(), Duration.ofHours(24));
            
            // Calculate behavior metrics
            double tradingFrequency = calculateTradingFrequency(recentTrades);
            double volumeDeviation = calculateVolumeDeviation(recentTrades);
            double timePatternAnomaly = analyzeTimePatterns(recentTrades);
            double priceDeviationScore = analyzePriceDeviations(recentTrades);
            
            // Detect specific fraud patterns
            boolean isPossibleWashTrading = detectWashTrading(recentTrades);
            boolean isPossibleFrontRunning = detectFrontRunning(trade, recentTrades);
            boolean isLayering = detectLayering(trade.getUserId());
            boolean isSpoofing = detectSpoofing(trade.getUserId());
            
            return TradingBehaviorScore.builder()
                .tradingFrequency(tradingFrequency)
                .volumeDeviation(volumeDeviation)
                .timePatternAnomaly(timePatternAnomaly)
                .priceDeviationScore(priceDeviationScore)
                .washTradingFlag(isPossibleWashTrading)
                .frontRunningFlag(isPossibleFrontRunning)
                .layeringFlag(isLayering)
                .spoofingFlag(isSpoofing)
                .overallScore(calculateOverallBehaviorScore(tradingFrequency, volumeDeviation, 
                    timePatternAnomaly, priceDeviationScore, isPossibleWashTrading, 
                    isPossibleFrontRunning, isLayering, isSpoofing))
                .build();
                
        } catch (Exception e) {
            logger.error("Error analyzing trading behavior for user: {}", trade.getUserId(), e);
            return TradingBehaviorScore.builder()
                .overallScore(0.0)
                .build();
        }
    }
    
    private boolean detectWashTrading(List<TradeEvent> trades) {
        try {
            // Group trades by symbol and time windows
            Map<String, List<TradeEvent>> tradesBySymbol = trades.stream()
                .collect(java.util.stream.Collectors.groupingBy(TradeEvent::getSymbol));
            
            for (List<TradeEvent> symbolTrades : tradesBySymbol.values()) {
                // Look for rapid buy-sell patterns with same quantities
                for (int i = 0; i < symbolTrades.size() - 1; i++) {
                    TradeEvent trade1 = symbolTrades.get(i);
                    TradeEvent trade2 = symbolTrades.get(i + 1);
                    
                    // Check for wash trading indicators
                    if (isWithinTimeWindow(trade1, trade2, Duration.ofMinutes(5)) &&
                        areOppositeSides(trade1, trade2) &&
                        areSimilarQuantities(trade1, trade2, 0.05) &&
                        areSimilarPrices(trade1, trade2, 0.01)) {
                        return true;
                    }
                }
            }
            return false;
            
        } catch (Exception e) {
            logger.error("Error detecting wash trading", e);
            return false;
        }
    }
    
    private boolean detectFrontRunning(TradeEvent currentTrade, List<TradeEvent> recentTrades) {
        try {
            // Look for patterns where small trades precede large trades
            return recentTrades.stream()
                .filter(trade -> trade.getTimestamp().isBefore(currentTrade.getTimestamp()))
                .filter(trade -> trade.getSymbol().equals(currentTrade.getSymbol()))
                .filter(trade -> Duration.between(trade.getTimestamp(), currentTrade.getTimestamp()).toMinutes() < 2)
                .anyMatch(trade -> 
                    trade.getQuantity().compareTo(currentTrade.getQuantity().multiply(BigDecimal.valueOf(0.1))) < 0 &&
                    trade.getSide().equals(currentTrade.getSide())
                );
                
        } catch (Exception e) {
            logger.error("Error detecting front running", e);
            return false;
        }
    }
    
    private boolean detectLayering(String userId) {
        try {
            // Get recent orders for the user
            List<OrderEvent> recentOrders = behaviorAnalysis.getRecentOrders(userId, Duration.ofHours(1));
            
            // Look for layering patterns - multiple orders at similar prices that are quickly cancelled
            long cancelledOrders = recentOrders.stream()
                .filter(order -> "CANCELLED".equals(order.getStatus()))
                .filter(order -> Duration.between(order.getTimestamp(), Instant.now()).toMinutes() < 10)
                .count();
            
            return cancelledOrders > 10 && cancelledOrders > recentOrders.size() * 0.7;
            
        } catch (Exception e) {
            logger.error("Error detecting layering for user: {}", userId, e);
            return false;
        }
    }
    
    private boolean detectSpoofing(String userId) {
        try {
            // Get recent orders and trades
            List<OrderEvent> recentOrders = behaviorAnalysis.getRecentOrders(userId, Duration.ofHours(1));
            List<TradeEvent> recentTrades = behaviorAnalysis.getRecentTrades(userId, Duration.ofHours(1));
            
            // Look for large orders that don't result in trades (spoofing)
            long largeOrders = recentOrders.stream()
                .filter(order -> order.getQuantity().compareTo(BigDecimal.valueOf(1000)) > 0)
                .filter(order -> order.getFilledQuantity() == null || order.getFilledQuantity().compareTo(BigDecimal.ZERO) == 0)
                .count();
            
            return largeOrders > 5 && recentTrades.size() < largeOrders * 0.1;
            
        } catch (Exception e) {
            logger.error("Error detecting spoofing for user: {}", userId, e);
            return false;
        }
    }
    
    private VelocityCheckResult performVelocityChecks(TradeEvent trade) {
        try {
            String userId = trade.getUserId();
            Instant now = Instant.now();
            
            // Check trading velocity (trades per minute)
            List<TradeEvent> lastMinuteTrades = behaviorAnalysis.getTradesInTimeRange(
                userId, now.minus(Duration.ofMinutes(1)), now);
            
            // Check volume velocity (volume per hour)
            List<TradeEvent> lastHourTrades = behaviorAnalysis.getTradesInTimeRange(
                userId, now.minus(Duration.ofHours(1)), now);
            
            BigDecimal hourlyVolume = lastHourTrades.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double tradeVelocity = lastMinuteTrades.size();
            double volumeVelocity = hourlyVolume.doubleValue();
            
            // Determine risk levels
            VelocityRiskLevel tradeRisk = tradeVelocity > 20 ? VelocityRiskLevel.HIGH :
                                        tradeVelocity > 10 ? VelocityRiskLevel.MEDIUM : VelocityRiskLevel.LOW;
            
            VelocityRiskLevel volumeRisk = volumeVelocity > 1000000 ? VelocityRiskLevel.HIGH :
                                          volumeVelocity > 500000 ? VelocityRiskLevel.MEDIUM : VelocityRiskLevel.LOW;
            
            return VelocityCheckResult.builder()
                .tradeVelocity(tradeVelocity)
                .volumeVelocity(volumeVelocity)
                .tradeRisk(tradeRisk)
                .volumeRisk(volumeRisk)
                .overallRisk(tradeRisk.ordinal() > volumeRisk.ordinal() ? tradeRisk : volumeRisk)
                .build();
                
        } catch (Exception e) {
            logger.error("Error performing velocity checks", e);
            return VelocityCheckResult.builder()
                .overallRisk(VelocityRiskLevel.LOW)
                .build();
        }
    }
    
    private OrderRiskAssessment analyzeOrderFraud(OrderEvent order) {
        try {
            double riskScore = 0.0;
            java.util.List<String> riskFactors = new java.util.ArrayList<>();
            
            // Check for unusually large orders
            if (order.getQuantity() != null && order.getQuantity().compareTo(BigDecimal.valueOf(10000)) > 0) {
                riskScore += 20.0;
                riskFactors.add("LARGE_ORDER_SIZE");
            }
            
            // Check for round number amounts (possible test/wash trading)
            if (order.getQuantity() != null && isRoundNumber(order.getQuantity())) {
                riskScore += 15.0;
                riskFactors.add("ROUND_NUMBER_QUANTITY");
            }
            
            // Check for orders at unusual prices
            if (order.getPrice() != null && isUnusualPrice(order.getSymbol(), order.getPrice())) {
                riskScore += 25.0;
                riskFactors.add("UNUSUAL_PRICE");
            }
            
            // Check user behavior patterns
            if (order.getUserId() != null) {
                boolean hasUnusualPattern = hasUnusualOrderPattern(order.getUserId());
                if (hasUnusualPattern) {
                    riskScore += 30.0;
                    riskFactors.add("UNUSUAL_USER_PATTERN");
                }
            }
            
            RiskLevel riskLevel = riskScore > 50 ? RiskLevel.HIGH :
                                 riskScore > 25 ? RiskLevel.MEDIUM : RiskLevel.LOW;
            
            return OrderRiskAssessment.builder()
                .orderId(order.getOrderId())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .riskFactors(riskFactors)
                .timestamp(Instant.now())
                .build();
                
        } catch (Exception e) {
            logger.error("Error analyzing order fraud", e);
            return OrderRiskAssessment.builder()
                .orderId(order.getOrderId())
                .riskLevel(RiskLevel.LOW)
                .riskScore(0.0)
                .riskFactors(List.of())
                .build();
        }
    }
    
    private ComprehensiveRiskAssessment combineRiskAssessments(
            RiskScore ruleBasedScore, AnomalyScore mlAnomalyScore, 
            TradingBehaviorScore behaviorScore, VelocityCheckResult velocityResult) {
        
        double combinedScore = 0.0;
        java.util.List<String> allRiskFactors = new java.util.ArrayList<>();
        
        // Weight the different scores
        combinedScore += ruleBasedScore.getScore() * 0.3;  // 30% weight for rules
        combinedScore += mlAnomalyScore.getScore() * 0.25; // 25% weight for ML
        combinedScore += behaviorScore.getOverallScore() * 0.25; // 25% weight for behavior
        combinedScore += velocityResult.getOverallRisk().ordinal() * 10 * 0.2; // 20% weight for velocity
        
        // Combine risk factors
        allRiskFactors.addAll(ruleBasedScore.getRiskFactors());
        allRiskFactors.addAll(mlAnomalyScore.getRiskFactors());
        allRiskFactors.addAll(behaviorScore.getRiskFactors());
        allRiskFactors.addAll(velocityResult.getRiskFactors());
        
        RiskLevel finalRiskLevel = combinedScore > 70 ? RiskLevel.HIGH :
                                  combinedScore > 40 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        
        return ComprehensiveRiskAssessment.builder()
            .combinedScore(combinedScore)
            .riskLevel(finalRiskLevel)
            .riskFactors(allRiskFactors)
            .ruleBasedScore(ruleBasedScore)
            .mlAnomalyScore(mlAnomalyScore)
            .behaviorScore(behaviorScore)
            .velocityResult(velocityResult)
            .timestamp(Instant.now())
            .build();
    }
    
    private void handleRiskAssessment(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        switch (assessment.getRiskLevel()) {
            case HIGH:
                // Freeze account and alert compliance team
                freezeUserAccount(trade.getUserId(), "High fraud risk detected");
                alertService.sendUrgentAlert(createHighRiskAlert(trade, assessment));
                // Cancel pending orders
                cancelUserOrders(trade.getUserId());
                logger.warn("HIGH RISK DETECTED - User: {}, Trade: {}, Score: {}", 
                          trade.getUserId(), trade.getTradeId(), assessment.getCombinedScore());
                break;
                
            case MEDIUM:
                // Enhanced monitoring and manual review
                flagForManualReview(trade, assessment);
                alertService.sendAlert(createMediumRiskAlert(trade, assessment));
                // Limit trading activity
                imposeTradingLimits(trade.getUserId());
                logger.info("MEDIUM RISK DETECTED - User: {}, Trade: {}, Score: {}", 
                          trade.getUserId(), trade.getTradeId(), assessment.getCombinedScore());
                break;
                
            case LOW:
                // Continue monitoring but allow trading
                logRiskAssessment(trade, assessment);
                logger.debug("LOW RISK - User: {}, Trade: {}, Score: {}", 
                           trade.getUserId(), trade.getTradeId(), assessment.getCombinedScore());
                break;
        }
        
        // Store assessment for ML model training and audit trail
        storeRiskAssessment(trade, assessment);
    }
    
    // Helper methods
    private boolean isWithinTimeWindow(TradeEvent trade1, TradeEvent trade2, Duration window) {
        return Math.abs(Duration.between(trade1.getTimestamp(), trade2.getTimestamp()).toMillis()) 
               <= window.toMillis();
    }
    
    private boolean areOppositeSides(TradeEvent trade1, TradeEvent trade2) {
        return (trade1.getSide().equals("BUY") && trade2.getSide().equals("SELL")) ||
               (trade1.getSide().equals("SELL") && trade2.getSide().equals("BUY"));
    }
    
    private boolean areSimilarQuantities(TradeEvent trade1, TradeEvent trade2, double tolerance) {
        if (trade1.getQuantity() == null || trade2.getQuantity() == null) return false;
        BigDecimal diff = trade1.getQuantity().subtract(trade2.getQuantity()).abs();
        BigDecimal avg = trade1.getQuantity().add(trade2.getQuantity()).divide(BigDecimal.valueOf(2));
        return diff.divide(avg, 4, java.math.RoundingMode.HALF_UP).doubleValue() <= tolerance;
    }
    
    private boolean areSimilarPrices(TradeEvent trade1, TradeEvent trade2, double tolerance) {
        if (trade1.getPrice() == null || trade2.getPrice() == null) return false;
        BigDecimal diff = trade1.getPrice().subtract(trade2.getPrice()).abs();
        BigDecimal avg = trade1.getPrice().add(trade2.getPrice()).divide(BigDecimal.valueOf(2));
        return diff.divide(avg, 4, java.math.RoundingMode.HALF_UP).doubleValue() <= tolerance;
    }
    
    private boolean isRoundNumber(BigDecimal number) {
        return number.remainder(BigDecimal.valueOf(100)).compareTo(BigDecimal.ZERO) == 0 ||
               number.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) == 0;
    }
    
    private boolean isUnusualPrice(String symbol, BigDecimal price) {
        // Simplified check - in reality, would compare against recent market prices
        return false;
    }
    
    private boolean hasUnusualOrderPattern(String userId) {
        // Simplified check - in reality, would analyze user's historical patterns
        return false;
    }
    
    private double calculateTradingFrequency(List<TradeEvent> trades) {
        if (trades.isEmpty()) return 0.0;
        return trades.size() / 24.0; // trades per hour over 24 hours
    }
    
    private double calculateVolumeDeviation(List<TradeEvent> trades) {
        if (trades.isEmpty()) return 0.0;
        
        List<Double> volumes = trades.stream()
            .map(trade -> trade.getAmount() != null ? trade.getAmount().doubleValue() : 0.0)
            .collect(java.util.stream.Collectors.toList());
        
        double mean = volumes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = volumes.stream()
            .mapToDouble(vol -> Math.pow(vol - mean, 2))
            .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double analyzeTimePatterns(List<TradeEvent> trades) {
        // Simplified implementation - analyze if trades are clustered in unusual time patterns
        return 0.0;
    }
    
    private double analyzePriceDeviations(List<TradeEvent> trades) {
        // Simplified implementation - analyze if trade prices deviate from market
        return 0.0;
    }
    
    private double calculateOverallBehaviorScore(double tradingFreq, double volumeDev, 
            double timeAnomaly, double priceDeviation, boolean washTrading, 
            boolean frontRunning, boolean layering, boolean spoofing) {
        
        double score = tradingFreq * 0.2 + volumeDev * 0.2 + timeAnomaly * 0.2 + priceDeviation * 0.2;
        
        if (washTrading) score += 30;
        if (frontRunning) score += 25;
        if (layering) score += 20;
        if (spoofing) score += 25;
        
        return Math.min(score, 100.0);
    }
    
    private BigDecimal calculateTransactionRiskScore(Map<String, Object> transactionData) {
        BigDecimal baseScore = new BigDecimal("20");
        
        // Check transaction amount
        if (transactionData.containsKey("amount")) {
            BigDecimal amount = new BigDecimal(transactionData.get("amount").toString());
            if (amount.compareTo(new BigDecimal("10000")) > 0) {
                baseScore = baseScore.add(new BigDecimal("30"));
            }
        }
        
        // Check for suspicious patterns
        if (transactionData.containsKey("ipAddress")) {
            String ip = transactionData.get("ipAddress").toString();
            if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
                baseScore = baseScore.add(new BigDecimal("10"));
            }
        }
        
        return baseScore.min(new BigDecimal("100"));
    }
    
    private List<String> checkFraudIndicators(Map<String, Object> transactionData) {
        List<String> indicators = new ArrayList<>();
        
        if (transactionData.containsKey("amount")) {
            BigDecimal amount = new BigDecimal(transactionData.get("amount").toString());
            if (amount.compareTo(new BigDecimal("50000")) > 0) {
                indicators.add("Large transaction amount");
            }
        }
        
        if (transactionData.containsKey("timestamp")) {
            // Check for unusual timing patterns
            indicators.add("Normal trading hours");
        }
        
        return indicators;
    }
    
    private String generateRecommendation(BigDecimal riskScore, boolean isFraudulent) {
        if (isFraudulent) {
            return "BLOCK - High fraud risk detected. Manual review required.";
        } else if (riskScore.compareTo(new BigDecimal("50")) > 0) {
            return "REVIEW - Medium fraud risk. Additional verification recommended.";
        } else {
            return "APPROVE - Low fraud risk. Transaction can proceed normally.";
        }
    }

    // Placeholder methods for actions
    private void freezeUserAccount(String userId, String reason) {
        logger.warn("FREEZING USER ACCOUNT: {} - Reason: {}", userId, reason);
    }
    
    private void cancelUserOrders(String userId) {
        logger.warn("CANCELLING ALL ORDERS for user: {}", userId);
    }
    
    private void flagForManualReview(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        logger.info("FLAGGING FOR MANUAL REVIEW: Trade {} - Risk Level: {}", 
                   trade.getTradeId(), assessment.getRiskLevel());
    }
    
    private void imposeTradingLimits(String userId) {
        logger.info("IMPOSING TRADING LIMITS for user: {}", userId);
    }
    
    private void logRiskAssessment(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        logger.debug("Risk assessment logged for trade: {}", trade.getTradeId());
    }
    
    private void storeRiskAssessment(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        logger.debug("Storing risk assessment for trade: {}", trade.getTradeId());
    }
    
    private FraudAlert createHighRiskAlert(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        return FraudAlert.builder()
            .type("HIGH_RISK_TRADE")
            .tradeId(trade.getTradeId())
            .userId(trade.getUserId())
            .riskScore(assessment.getCombinedScore())
            .riskFactors(assessment.getRiskFactors())
            .recommendedAction("FREEZE_ACCOUNT")
            .timestamp(Instant.now())
            .build();
    }
    
    private FraudAlert createMediumRiskAlert(TradeEvent trade, ComprehensiveRiskAssessment assessment) {
        return FraudAlert.builder()
            .type("MEDIUM_RISK_TRADE")
            .tradeId(trade.getTradeId())
            .userId(trade.getUserId())
            .riskScore(assessment.getCombinedScore())
            .riskFactors(assessment.getRiskFactors())
            .recommendedAction("ENHANCED_MONITORING")
            .timestamp(Instant.now())
            .build();
    }
}
