package com.bigdata.crypto.risk;

import com.bigdata.crypto.model.MarketData;
import com.bigdata.crypto.model.TradeEvent;
import com.bigdata.crypto.model.OrderEvent;
import com.bigdata.crypto.risk.RiskModels.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive Risk Management Service for cryptocurrency trading
 * Provides real-time risk assessment, position management, and exposure monitoring
 */
@Service
@Slf4j
public class CryptoRiskManagementService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String POSITION_KEY_PREFIX = "position:";
    private static final String RISK_METRICS_KEY_PREFIX = "risk:";
    private static final String EXPOSURE_KEY_PREFIX = "exposure:";
    private static final String VAR_KEY_PREFIX = "var:";

    /**
     * Calculate portfolio risk metrics including VaR, exposure, and concentration
     */
    public RiskAssessment calculatePortfolioRisk(String portfolioId) {
        try {
            Map<String, PositionInfo> positions = getPortfolioPositions(portfolioId);
            
            RiskAssessment assessment = RiskAssessment.builder()
                    .portfolioId(portfolioId)
                    .timestamp(Instant.now())
                    .build();

            // Calculate Value at Risk (VaR)
            BigDecimal var95 = calculateVaR(positions, 0.95);
            BigDecimal var99 = calculateVaR(positions, 0.99);
            
            // Calculate total exposure
            BigDecimal totalExposure = calculateTotalExposure(positions);
            
            // Calculate concentration risk
            Map<String, BigDecimal> concentrationByAsset = calculateConcentrationRisk(positions);
            
            // Calculate maximum drawdown
            BigDecimal maxDrawdown = calculateMaxDrawdown(portfolioId);
            
            // Calculate Sharpe ratio
            BigDecimal sharpeRatio = calculateSharpeRatio(portfolioId);

            assessment.setVar95(var95);
            assessment.setVar99(var99);
            assessment.setTotalExposure(totalExposure);
            assessment.setConcentrationByAsset(concentrationByAsset);
            assessment.setMaxDrawdown(maxDrawdown);
            assessment.setSharpeRatio(sharpeRatio);
            assessment.setRiskScore(calculateOverallRiskScore(assessment));

            // Cache risk assessment
            cacheRiskAssessment(assessment);
            
            return assessment;
            
        } catch (Exception e) {
            log.error("Error calculating portfolio risk for {}: {}", portfolioId, e.getMessage(), e);
            throw new RuntimeException("Risk calculation failed", e);
        }
    }

    /**
     * Real-time position risk monitoring
     */
    public PositionRisk assessPositionRisk(TradeEvent trade) {
        try {
            String positionKey = POSITION_KEY_PREFIX + trade.getUserId() + ":" + trade.getSymbol();
            PositionInfo position = getCurrentPosition(positionKey);
            
            // Update position with new trade
            position = updatePositionWithTrade(position, trade);
            
            // Calculate position-specific risks
            PositionRisk risk = PositionRisk.builder()
                    .userId(trade.getUserId())
                    .symbol(trade.getSymbol())
                    .currentPosition(position.getQuantity())
                    .averagePrice(position.getAveragePrice())
                    .unrealizedPnL(calculateUnrealizedPnL(position, getCurrentPrice(trade.getSymbol())))
                    .leverage(calculateLeverage(position))
                    .marginUsed(calculateMarginUsed(position))
                    .liquidationPrice(calculateLiquidationPrice(position))
                    .riskLevel(assessRiskLevel(position))
                    .timestamp(Instant.now())
                    .build();

            // Cache updated position and risk
            redisTemplate.opsForValue().set(positionKey, position, 24, TimeUnit.HOURS);
            cachePositionRisk(risk);
            
            return risk;
            
        } catch (Exception e) {
            log.error("Error assessing position risk for trade {}: {}", trade.getId(), e.getMessage(), e);
            throw new RuntimeException("Position risk assessment failed", e);
        }
    }

    /**
     * Market risk analysis based on volatility and correlation
     */
    public MarketRiskAnalysis analyzeMarketRisk(List<String> symbols, int lookbackDays) {
        try {
            Map<String, List<BigDecimal>> priceHistory = getHistoricalPrices(symbols, lookbackDays);
            
            MarketRiskAnalysis analysis = MarketRiskAnalysis.builder()
                    .symbols(symbols)
                    .analysisDate(Instant.now())
                    .lookbackDays(lookbackDays)
                    .build();

            // Calculate volatilities
            Map<String, BigDecimal> volatilities = new HashMap<>();
            for (String symbol : symbols) {
                BigDecimal volatility = calculateVolatility(priceHistory.get(symbol));
                volatilities.put(symbol, volatility);
            }
            analysis.setVolatilities(volatilities);

            // Calculate correlation matrix
            Map<String, Map<String, BigDecimal>> correlationMatrix = calculateCorrelationMatrix(priceHistory);
            analysis.setCorrelationMatrix(correlationMatrix);

            // Calculate beta (relative to BTC if available)
            if (symbols.contains("BTC-USD")) {
                Map<String, BigDecimal> betas = calculateBetas(priceHistory, "BTC-USD");
                analysis.setBetas(betas);
            }

            // Assess market regime
            analysis.setMarketRegime(assessMarketRegime(priceHistory));
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Error analyzing market risk: {}", e.getMessage(), e);
            throw new RuntimeException("Market risk analysis failed", e);
        }
    }

    /**
     * Stress testing scenarios
     */
    public StressTestResult performStressTest(String portfolioId, StressTestScenario scenario) {
        try {
            Map<String, PositionInfo> positions = getPortfolioPositions(portfolioId);
            
            StressTestResult result = StressTestResult.builder()
                    .portfolioId(portfolioId)
                    .scenario(scenario)
                    .testDate(Instant.now())
                    .build();

            BigDecimal totalPnL = BigDecimal.ZERO;
            Map<String, BigDecimal> pnlByAsset = new HashMap<>();

            for (Map.Entry<String, PositionInfo> entry : positions.entrySet()) {
                String symbol = entry.getKey();
                PositionInfo position = entry.getValue();
                
                // Apply stress scenario to position
                BigDecimal stressedPrice = applyStressScenario(getCurrentPrice(symbol), scenario, symbol);
                BigDecimal positionPnL = calculatePnLAtPrice(position, stressedPrice);
                
                pnlByAsset.put(symbol, positionPnL);
                totalPnL = totalPnL.add(positionPnL);
            }

            result.setTotalPnL(totalPnL);
            result.setPnlByAsset(pnlByAsset);
            result.setSeverity(assessStressSeverity(totalPnL, scenario));

            return result;
            
        } catch (Exception e) {
            log.error("Error performing stress test: {}", e.getMessage(), e);
            throw new RuntimeException("Stress test failed", e);
        }
    }

    // Private helper methods

    private Map<String, PositionInfo> getPortfolioPositions(String portfolioId) {
        // Implementation to retrieve positions from Redis/MongoDB
        String pattern = POSITION_KEY_PREFIX + portfolioId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        Map<String, PositionInfo> positions = new HashMap<>();
        
        if (keys != null) {
            for (String key : keys) {
                PositionInfo position = (PositionInfo) redisTemplate.opsForValue().get(key);
                if (position != null) {
                    String symbol = key.substring(key.lastIndexOf(':') + 1);
                    positions.put(symbol, position);
                }
            }
        }
        
        return positions;
    }

    private BigDecimal calculateVaR(Map<String, PositionInfo> positions, double confidenceLevel) {
        // Simplified VaR calculation using historical simulation
        // In production, this would use more sophisticated methods
        BigDecimal totalValue = BigDecimal.ZERO;
        for (PositionInfo position : positions.values()) {
            totalValue = totalValue.add(position.getMarketValue());
        }
        
        // Assume daily volatility of 3% for crypto markets
        BigDecimal volatility = new BigDecimal("0.03");
        double zScore = confidenceLevel == 0.95 ? 1.645 : 2.326; // 95% or 99%
        
        return totalValue.multiply(volatility).multiply(BigDecimal.valueOf(zScore));
    }

    private BigDecimal calculateTotalExposure(Map<String, PositionInfo> positions) {
        return positions.values().stream()
                .map(PositionInfo::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> calculateConcentrationRisk(Map<String, PositionInfo> positions) {
        BigDecimal totalValue = calculateTotalExposure(positions);
        Map<String, BigDecimal> concentration = new HashMap<>();
        
        for (Map.Entry<String, PositionInfo> entry : positions.entrySet()) {
            BigDecimal percentage = entry.getValue().getMarketValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP);
            concentration.put(entry.getKey(), percentage);
        }
        
        return concentration;
    }

    private BigDecimal calculateMaxDrawdown(String portfolioId) {
        // Simplified implementation - would use historical portfolio values
        return new BigDecimal("0.15"); // 15% max drawdown assumption
    }

    private BigDecimal calculateSharpeRatio(String portfolioId) {
        // Simplified implementation - would calculate from historical returns
        return new BigDecimal("1.2"); // Assumption
    }

    private RiskLevel calculateOverallRiskScore(RiskAssessment assessment) {
        // Simplified risk scoring
        BigDecimal riskScore = BigDecimal.ZERO;
        
        // VaR contribution
        if (assessment.getVar95().compareTo(assessment.getTotalExposure().multiply(new BigDecimal("0.1"))) > 0) {
            riskScore = riskScore.add(BigDecimal.valueOf(3));
        }
        
        // Concentration risk
        for (BigDecimal concentration : assessment.getConcentrationByAsset().values()) {
            if (concentration.compareTo(new BigDecimal("0.3")) > 0) {
                riskScore = riskScore.add(BigDecimal.valueOf(2));
            }
        }
        
        if (riskScore.compareTo(BigDecimal.valueOf(5)) >= 0) return RiskLevel.HIGH;
        if (riskScore.compareTo(BigDecimal.valueOf(3)) >= 0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private PositionInfo getCurrentPosition(String positionKey) {
        PositionInfo position = (PositionInfo) redisTemplate.opsForValue().get(positionKey);
        return position != null ? position : new PositionInfo();
    }

    private PositionInfo updatePositionWithTrade(PositionInfo position, TradeEvent trade) {
        // Update position with new trade
        BigDecimal newQuantity = position.getQuantity().add(
            trade.getSide().equals("buy") ? trade.getQuantity() : trade.getQuantity().negate()
        );
        
        // Update average price using weighted average
        if (newQuantity.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal totalCost = position.getQuantity().multiply(position.getAveragePrice())
                    .add(trade.getQuantity().multiply(trade.getPrice()));
            BigDecimal newAvgPrice = totalCost.divide(newQuantity.abs(), 8, RoundingMode.HALF_UP);
            position.setAveragePrice(newAvgPrice);
        }
        
        position.setQuantity(newQuantity);
        position.setLastUpdateTime(Instant.now());
        
        return position;
    }

    private BigDecimal getCurrentPrice(String symbol) {
        // Retrieve current price from cache or external API
        String priceKey = "price:" + symbol;
        Object price = redisTemplate.opsForValue().get(priceKey);
        return price != null ? new BigDecimal(price.toString()) : BigDecimal.ZERO;
    }

    private BigDecimal calculateUnrealizedPnL(PositionInfo position, BigDecimal currentPrice) {
        return position.getQuantity().multiply(currentPrice.subtract(position.getAveragePrice()));
    }

    private BigDecimal calculateLeverage(PositionInfo position) {
        // Simplified leverage calculation
        return position.getMarketValue().divide(position.getCollateral(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMarginUsed(PositionInfo position) {
        // Simplified margin calculation (10% margin requirement)
        return position.getMarketValue().multiply(new BigDecimal("0.1"));
    }

    private BigDecimal calculateLiquidationPrice(PositionInfo position) {
        // Simplified liquidation price (80% of average price for long positions)
        return position.getAveragePrice().multiply(new BigDecimal("0.8"));
    }

    private RiskLevel assessRiskLevel(PositionInfo position) {
        BigDecimal leverage = calculateLeverage(position);
        if (leverage.compareTo(new BigDecimal("5")) > 0) return RiskLevel.HIGH;
        if (leverage.compareTo(new BigDecimal("3")) > 0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private void cacheRiskAssessment(RiskAssessment assessment) {
        String key = RISK_METRICS_KEY_PREFIX + assessment.getPortfolioId();
        redisTemplate.opsForValue().set(key, assessment, 1, TimeUnit.HOURS);
    }

    private void cachePositionRisk(PositionRisk risk) {
        String key = RISK_METRICS_KEY_PREFIX + "position:" + risk.getUserId() + ":" + risk.getSymbol();
        redisTemplate.opsForValue().set(key, risk, 6, TimeUnit.HOURS);
    }

    // Additional helper methods for market risk analysis
    private Map<String, List<BigDecimal>> getHistoricalPrices(List<String> symbols, int days) {
        // Implementation to retrieve historical prices
        Map<String, List<BigDecimal>> priceHistory = new HashMap<>();
        // Simplified - would integrate with actual price data source
        return priceHistory;
    }

    private BigDecimal calculateVolatility(List<BigDecimal> prices) {
        // Calculate standard deviation of returns
        if (prices.size() < 2) return BigDecimal.ZERO;
        
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal return_ = prices.get(i).divide(prices.get(i-1), 8, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE);
            returns.add(return_);
        }
        
        // Calculate standard deviation
        BigDecimal mean = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size() - 1), 8, RoundingMode.HALF_UP);
        
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    private Map<String, Map<String, BigDecimal>> calculateCorrelationMatrix(Map<String, List<BigDecimal>> priceHistory) {
        // Simplified correlation matrix calculation
        Map<String, Map<String, BigDecimal>> correlationMatrix = new HashMap<>();
        // Implementation would calculate Pearson correlation coefficients
        return correlationMatrix;
    }

    private Map<String, BigDecimal> calculateBetas(Map<String, List<BigDecimal>> priceHistory, String benchmarkSymbol) {
        // Calculate beta coefficients relative to benchmark
        Map<String, BigDecimal> betas = new HashMap<>();
        // Implementation would use regression analysis
        return betas;
    }

    private MarketRegime assessMarketRegime(Map<String, List<BigDecimal>> priceHistory) {
        // Assess current market conditions
        // This could use various indicators like VIX equivalent, trend analysis, etc.
        return MarketRegime.NORMAL; // Simplified
    }

    private BigDecimal applyStressScenario(BigDecimal currentPrice, StressTestScenario scenario, String symbol) {
        // Apply stress scenario adjustments
        switch (scenario.getType()) {
            case MARKET_CRASH:
                return currentPrice.multiply(new BigDecimal("0.7")); // 30% drop
            case VOLATILITY_SPIKE:
                return currentPrice.multiply(new BigDecimal("0.85")); // 15% drop with high vol
            case LIQUIDITY_CRISIS:
                return currentPrice.multiply(new BigDecimal("0.8")); // 20% drop
            default:
                return currentPrice;
        }
    }

    private BigDecimal calculatePnLAtPrice(PositionInfo position, BigDecimal price) {
        return position.getQuantity().multiply(price.subtract(position.getAveragePrice()));
    }

    private StressSeverity assessStressSeverity(BigDecimal totalPnL, StressTestScenario scenario) {
        BigDecimal lossThreshold = new BigDecimal("-100000"); // $100k loss threshold
        
        if (totalPnL.compareTo(lossThreshold) < 0) {
            return StressSeverity.SEVERE;
        } else if (totalPnL.compareTo(lossThreshold.multiply(new BigDecimal("0.5"))) < 0) {
            return StressSeverity.MODERATE;
        } else {
            return StressSeverity.MILD;
        }
    }
}
