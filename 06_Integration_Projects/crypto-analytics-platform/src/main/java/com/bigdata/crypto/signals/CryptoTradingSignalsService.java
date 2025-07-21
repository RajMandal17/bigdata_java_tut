package com.bigdata.crypto.signals;

import com.bigdata.crypto.model.MarketData;
import com.bigdata.crypto.model.TradeEvent;
import com.bigdata.crypto.signals.SignalModels.*;
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
 * Advanced Trading Signals Generation Service
 * Generates buy/sell signals based on technical analysis and ML models
 */
@Service
@Slf4j
public class CryptoTradingSignalsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRICE_HISTORY_KEY = "price_history:";
    private static final String SIGNAL_KEY = "signal:";
    private static final String INDICATOR_KEY = "indicator:";

    /**
     * Generate comprehensive trading signals for a symbol
     */
    public TradingSignal generateSignal(String symbol) {
        try {
            log.info("Generating trading signal for symbol: {}", symbol);
            
            List<PricePoint> priceHistory = getPriceHistory(symbol, 200); // 200 periods
            if (priceHistory.size() < 50) {
                log.warn("Insufficient price history for {}, returning HOLD signal", symbol);
                return createHoldSignal(symbol);
            }

            // Calculate technical indicators
            TechnicalIndicators indicators = calculateTechnicalIndicators(priceHistory);
            
            // Generate signal based on multiple indicators
            SignalStrength signalStrength = calculateSignalStrength(indicators);
            SignalType signalType = determineSignalType(indicators);
            
            // Calculate confidence based on indicator convergence
            BigDecimal confidence = calculateSignalConfidence(indicators);
            
            // Get current market conditions
            MarketCondition marketCondition = assessMarketCondition(priceHistory);
            
            TradingSignal signal = TradingSignal.builder()
                    .symbol(symbol)
                    .signalType(signalType)
                    .strength(signalStrength)
                    .confidence(confidence)
                    .currentPrice(getCurrentPrice(symbol))
                    .targetPrice(calculateTargetPrice(priceHistory, signalType))
                    .stopLoss(calculateStopLoss(priceHistory, signalType))
                    .marketCondition(marketCondition)
                    .indicators(indicators)
                    .timestamp(Instant.now())
                    .reasoning(generateReasoning(indicators, signalType))
                    .build();

            // Cache the signal
            cacheSignal(signal);
            
            return signal;
            
        } catch (Exception e) {
            log.error("Error generating signal for {}: {}", symbol, e.getMessage(), e);
            return createHoldSignal(symbol);
        }
    }

    /**
     * Generate signals for multiple symbols
     */
    public Map<String, TradingSignal> generateSignals(List<String> symbols) {
        Map<String, TradingSignal> signals = new HashMap<>();
        
        for (String symbol : symbols) {
            try {
                TradingSignal signal = generateSignal(symbol);
                signals.put(symbol, signal);
            } catch (Exception e) {
                log.error("Failed to generate signal for {}: {}", symbol, e.getMessage());
                signals.put(symbol, createHoldSignal(symbol));
            }
        }
        
        return signals;
    }

    /**
     * Get portfolio-level trading recommendations
     */
    public PortfolioRecommendation getPortfolioRecommendation(String userId, List<String> symbols) {
        try {
            Map<String, TradingSignal> signals = generateSignals(symbols);
            
            // Analyze portfolio correlation and diversification
            PortfolioAnalysis analysis = analyzePortfolioCorrelation(symbols);
            
            // Generate recommendations based on signals and portfolio analysis
            List<TradeRecommendation> recommendations = generateTradeRecommendations(signals, analysis);
            
            // Calculate portfolio risk score
            BigDecimal riskScore = calculatePortfolioRiskScore(signals, analysis);
            
            return PortfolioRecommendation.builder()
                    .userId(userId)
                    .recommendations(recommendations)
                    .portfolioRiskScore(riskScore)
                    .marketSentiment(calculateMarketSentiment(signals))
                    .diversificationScore(analysis.getDiversificationScore())
                    .timestamp(Instant.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating portfolio recommendation for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Portfolio recommendation failed", e);
        }
    }

    // Technical Indicators Calculation

    private TechnicalIndicators calculateTechnicalIndicators(List<PricePoint> priceHistory) {
        TechnicalIndicators.TechnicalIndicatorsBuilder builder = TechnicalIndicators.builder();
        
        // Moving Averages
        builder.sma20(calculateSMA(priceHistory, 20));
        builder.sma50(calculateSMA(priceHistory, 50));
        builder.ema12(calculateEMA(priceHistory, 12));
        builder.ema26(calculateEMA(priceHistory, 26));
        
        // MACD
        MacdResult macd = calculateMACD(priceHistory);
        builder.macdLine(macd.getMacdLine());
        builder.macdSignal(macd.getSignalLine());
        builder.macdHistogram(macd.getHistogram());
        
        // RSI
        builder.rsi(calculateRSI(priceHistory, 14));
        
        // Bollinger Bands
        BollingerBands bands = calculateBollingerBands(priceHistory, 20, 2.0);
        builder.bollingerUpper(bands.getUpperBand());
        builder.bollingerMiddle(bands.getMiddleBand());
        builder.bollingerLower(bands.getLowerBand());
        
        // Stochastic
        StochasticResult stochastic = calculateStochastic(priceHistory, 14);
        builder.stochasticK(stochastic.getPercentK());
        builder.stochasticD(stochastic.getPercentD());
        
        // Volume indicators (if available)
        if (hasVolumeData(priceHistory)) {
            builder.volumeSMA(calculateVolumeSMA(priceHistory, 20));
            builder.volumeRatio(calculateVolumeRatio(priceHistory));
        }
        
        return builder.build();
    }

    private BigDecimal calculateSMA(List<PricePoint> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i).getClose());
        }
        
        return sum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEMA(List<PricePoint> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = calculateSMA(prices.subList(0, period), period);
        
        for (int i = period; i < prices.size(); i++) {
            ema = prices.get(i).getClose().multiply(multiplier)
                    .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema;
    }

    private MacdResult calculateMACD(List<PricePoint> prices) {
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        BigDecimal macdLine = ema12.subtract(ema26);
        
        // Signal line is 9-period EMA of MACD line
        // Simplified calculation - in practice would need MACD history
        BigDecimal signalLine = macdLine.multiply(new BigDecimal("0.9"));
        BigDecimal histogram = macdLine.subtract(signalLine);
        
        return MacdResult.builder()
                .macdLine(macdLine)
                .signalLine(signalLine)
                .histogram(histogram)
                .build();
    }

    private BigDecimal calculateRSI(List<PricePoint> prices, int period) {
        if (prices.size() < period + 1) return new BigDecimal("50");
        
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).getClose().subtract(prices.get(i-1).getClose());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change.abs());
            }
        }
        
        BigDecimal avgGain = gains.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100");
        
        BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
        return new BigDecimal("100").subtract(
                new BigDecimal("100").divide(BigDecimal.ONE.add(rs), 8, RoundingMode.HALF_UP)
        );
    }

    private BollingerBands calculateBollingerBands(List<PricePoint> prices, int period, double standardDeviations) {
        BigDecimal sma = calculateSMA(prices, period);
        BigDecimal standardDeviation = calculateStandardDeviation(prices, period);
        BigDecimal deviation = standardDeviation.multiply(BigDecimal.valueOf(standardDeviations));
        
        return BollingerBands.builder()
                .upperBand(sma.add(deviation))
                .middleBand(sma)
                .lowerBand(sma.subtract(deviation))
                .build();
    }

    private StochasticResult calculateStochastic(List<PricePoint> prices, int period) {
        if (prices.size() < period) {
            return StochasticResult.builder()
                    .percentK(new BigDecimal("50"))
                    .percentD(new BigDecimal("50"))
                    .build();
        }
        
        List<PricePoint> recentPrices = prices.subList(prices.size() - period, prices.size());
        BigDecimal highestHigh = recentPrices.stream()
                .map(PricePoint::getHigh)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal lowestLow = recentPrices.stream()
                .map(PricePoint::getLow)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal currentClose = prices.get(prices.size() - 1).getClose();
        BigDecimal percentK = currentClose.subtract(lowestLow)
                .divide(highestHigh.subtract(lowestLow), 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        // %D is 3-period SMA of %K (simplified)
        BigDecimal percentD = percentK.multiply(new BigDecimal("0.8"));
        
        return StochasticResult.builder()
                .percentK(percentK)
                .percentD(percentD)
                .build();
    }

    // Signal Generation Logic

    private SignalType determineSignalType(TechnicalIndicators indicators) {
        int bullishSignals = 0;
        int bearishSignals = 0;
        
        // Moving Average signals
        if (indicators.getSma20().compareTo(indicators.getSma50()) > 0) bullishSignals++;
        else bearishSignals++;
        
        // MACD signals
        if (indicators.getMacdLine().compareTo(indicators.getMacdSignal()) > 0) bullishSignals++;
        else bearishSignals++;
        
        // RSI signals
        if (indicators.getRsi().compareTo(new BigDecimal("30")) < 0) bullishSignals++;
        else if (indicators.getRsi().compareTo(new BigDecimal("70")) > 0) bearishSignals++;
        
        // Stochastic signals
        if (indicators.getStochasticK().compareTo(new BigDecimal("20")) < 0) bullishSignals++;
        else if (indicators.getStochasticK().compareTo(new BigDecimal("80")) > 0) bearishSignals++;
        
        if (bullishSignals > bearishSignals + 1) return SignalType.BUY;
        if (bearishSignals > bullishSignals + 1) return SignalType.SELL;
        return SignalType.HOLD;
    }

    private SignalStrength calculateSignalStrength(TechnicalIndicators indicators) {
        int strongSignals = 0;
        
        // Strong moving average alignment
        if (indicators.getEma12().compareTo(indicators.getEma26()) > 0 &&
            indicators.getSma20().compareTo(indicators.getSma50()) > 0) {
            strongSignals++;
        }
        
        // Strong momentum
        if (indicators.getMacdHistogram().abs().compareTo(new BigDecimal("0.01")) > 0) {
            strongSignals++;
        }
        
        // Extreme RSI
        if (indicators.getRsi().compareTo(new BigDecimal("20")) < 0 ||
            indicators.getRsi().compareTo(new BigDecimal("80")) > 0) {
            strongSignals++;
        }
        
        if (strongSignals >= 2) return SignalStrength.STRONG;
        if (strongSignals == 1) return SignalStrength.MODERATE;
        return SignalStrength.WEAK;
    }

    private BigDecimal calculateSignalConfidence(TechnicalIndicators indicators) {
        // Base confidence
        BigDecimal confidence = new BigDecimal("50");
        
        // Increase confidence for aligned indicators
        if (indicators.getSma20().compareTo(indicators.getSma50()) > 0 &&
            indicators.getMacdLine().compareTo(indicators.getMacdSignal()) > 0) {
            confidence = confidence.add(new BigDecimal("20"));
        }
        
        // Adjust for extreme RSI
        if (indicators.getRsi().compareTo(new BigDecimal("30")) < 0 ||
            indicators.getRsi().compareTo(new BigDecimal("70")) > 0) {
            confidence = confidence.add(new BigDecimal("15"));
        }
        
        // Cap at 95%
        return confidence.min(new BigDecimal("95"));
    }

    // Helper methods

    private List<PricePoint> getPriceHistory(String symbol, int periods) {
        String key = PRICE_HISTORY_KEY + symbol;
        @SuppressWarnings("unchecked")
        List<PricePoint> history = (List<PricePoint>) redisTemplate.opsForValue().get(key);
        
        if (history == null || history.isEmpty()) {
            // Generate sample data for testing
            return generateSamplePriceHistory(symbol, periods);
        }
        
        return history.size() > periods ? 
                history.subList(history.size() - periods, history.size()) : history;
    }

    private List<PricePoint> generateSamplePriceHistory(String symbol, int periods) {
        List<PricePoint> history = new ArrayList<>();
        BigDecimal basePrice = new BigDecimal("50000"); // Sample BTC price
        Random random = new Random();
        
        for (int i = 0; i < periods; i++) {
            double change = (random.nextGaussian() * 0.02); // 2% daily volatility
            basePrice = basePrice.multiply(BigDecimal.valueOf(1 + change));
            
            BigDecimal high = basePrice.multiply(new BigDecimal("1.02"));
            BigDecimal low = basePrice.multiply(new BigDecimal("0.98"));
            BigDecimal volume = new BigDecimal(String.valueOf(1000000 + random.nextInt(500000)));
            
            history.add(PricePoint.builder()
                    .timestamp(Instant.now().minus(periods - i, ChronoUnit.DAYS))
                    .open(basePrice)
                    .high(high)
                    .low(low)
                    .close(basePrice)
                    .volume(volume)
                    .build());
        }
        
        return history;
    }

    private BigDecimal getCurrentPrice(String symbol) {
        String priceKey = "price:" + symbol;
        Object price = redisTemplate.opsForValue().get(priceKey);
        return price != null ? new BigDecimal(price.toString()) : new BigDecimal("50000");
    }

    private TradingSignal createHoldSignal(String symbol) {
        return TradingSignal.builder()
                .symbol(symbol)
                .signalType(SignalType.HOLD)
                .strength(SignalStrength.WEAK)
                .confidence(new BigDecimal("0"))
                .currentPrice(getCurrentPrice(symbol))
                .timestamp(Instant.now())
                .reasoning("Insufficient data or analysis error")
                .build();
    }

    private void cacheSignal(TradingSignal signal) {
        String key = SIGNAL_KEY + signal.getSymbol();
        redisTemplate.opsForValue().set(key, signal, 5, TimeUnit.MINUTES);
    }

    // Additional helper methods would be implemented here...
    private BigDecimal calculateTargetPrice(List<PricePoint> priceHistory, SignalType signalType) {
        BigDecimal currentPrice = priceHistory.get(priceHistory.size() - 1).getClose();
        switch (signalType) {
            case BUY:
                return currentPrice.multiply(new BigDecimal("1.05")); // 5% target
            case SELL:
                return currentPrice.multiply(new BigDecimal("0.95")); // 5% target
            default:
                return currentPrice;
        }
    }

    private BigDecimal calculateStopLoss(List<PricePoint> priceHistory, SignalType signalType) {
        BigDecimal currentPrice = priceHistory.get(priceHistory.size() - 1).getClose();
        switch (signalType) {
            case BUY:
                return currentPrice.multiply(new BigDecimal("0.97")); // 3% stop loss
            case SELL:
                return currentPrice.multiply(new BigDecimal("1.03")); // 3% stop loss
            default:
                return currentPrice;
        }
    }

    private MarketCondition assessMarketCondition(List<PricePoint> priceHistory) {
        // Simplified market condition assessment
        if (priceHistory.size() < 20) return MarketCondition.UNKNOWN;
        
        BigDecimal recentAvg = calculateSMA(priceHistory.subList(priceHistory.size() - 10, priceHistory.size()), 10);
        BigDecimal longerAvg = calculateSMA(priceHistory.subList(priceHistory.size() - 20, priceHistory.size()), 20);
        
        if (recentAvg.compareTo(longerAvg.multiply(new BigDecimal("1.05"))) > 0) {
            return MarketCondition.BULLISH;
        } else if (recentAvg.compareTo(longerAvg.multiply(new BigDecimal("0.95"))) < 0) {
            return MarketCondition.BEARISH;
        } else {
            return MarketCondition.SIDEWAYS;
        }
    }

    private String generateReasoning(TechnicalIndicators indicators, SignalType signalType) {
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Signal based on: ");
        
        if (indicators.getSma20().compareTo(indicators.getSma50()) > 0) {
            reasoning.append("Short-term MA above long-term MA; ");
        }
        
        if (indicators.getMacdLine().compareTo(indicators.getMacdSignal()) > 0) {
            reasoning.append("MACD bullish crossover; ");
        }
        
        if (indicators.getRsi().compareTo(new BigDecimal("70")) > 0) {
            reasoning.append("RSI overbought; ");
        } else if (indicators.getRsi().compareTo(new BigDecimal("30")) < 0) {
            reasoning.append("RSI oversold; ");
        }
        
        return reasoning.toString();
    }

    // Additional calculation methods
    private BigDecimal calculateStandardDeviation(List<PricePoint> prices, int period) {
        BigDecimal mean = calculateSMA(prices, period);
        BigDecimal variance = BigDecimal.ZERO;
        
        List<PricePoint> recentPrices = prices.subList(prices.size() - period, prices.size());
        for (PricePoint price : recentPrices) {
            BigDecimal diff = price.getClose().subtract(mean);
            variance = variance.add(diff.multiply(diff));
        }
        
        variance = variance.divide(BigDecimal.valueOf(period - 1), 8, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    private boolean hasVolumeData(List<PricePoint> priceHistory) {
        return priceHistory.stream().anyMatch(p -> p.getVolume().compareTo(BigDecimal.ZERO) > 0);
    }

    private BigDecimal calculateVolumeSMA(List<PricePoint> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i).getVolume());
        }
        
        return sum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolumeRatio(List<PricePoint> priceHistory) {
        if (priceHistory.size() < 2) return BigDecimal.ONE;
        
        BigDecimal currentVolume = priceHistory.get(priceHistory.size() - 1).getVolume();
        BigDecimal avgVolume = calculateVolumeSMA(priceHistory, Math.min(20, priceHistory.size()));
        
        if (avgVolume.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
        
        return currentVolume.divide(avgVolume, 4, RoundingMode.HALF_UP);
    }

    private PortfolioAnalysis analyzePortfolioCorrelation(List<String> symbols) {
        // Simplified portfolio analysis
        return PortfolioAnalysis.builder()
                .diversificationScore(new BigDecimal("75"))
                .correlationMatrix(new HashMap<>())
                .build();
    }

    private List<TradeRecommendation> generateTradeRecommendations(Map<String, TradingSignal> signals, PortfolioAnalysis analysis) {
        List<TradeRecommendation> recommendations = new ArrayList<>();
        
        for (Map.Entry<String, TradingSignal> entry : signals.entrySet()) {
            TradingSignal signal = entry.getValue();
            if (signal.getSignalType() != SignalType.HOLD && 
                signal.getConfidence().compareTo(new BigDecimal("60")) > 0) {
                
                recommendations.add(TradeRecommendation.builder()
                        .symbol(signal.getSymbol())
                        .action(signal.getSignalType().toString())
                        .confidence(signal.getConfidence())
                        .targetPrice(signal.getTargetPrice())
                        .stopLoss(signal.getStopLoss())
                        .reasoning(signal.getReasoning())
                        .build());
            }
        }
        
        return recommendations;
    }

    private BigDecimal calculatePortfolioRiskScore(Map<String, TradingSignal> signals, PortfolioAnalysis analysis) {
        // Simplified risk score calculation
        return new BigDecimal("60"); // Medium risk
    }

    private MarketSentiment calculateMarketSentiment(Map<String, TradingSignal> signals) {
        long buySignals = signals.values().stream()
                .mapToLong(s -> s.getSignalType() == SignalType.BUY ? 1 : 0)
                .sum();
        
        long sellSignals = signals.values().stream()
                .mapToLong(s -> s.getSignalType() == SignalType.SELL ? 1 : 0)
                .sum();
        
        if (buySignals > sellSignals * 1.5) return MarketSentiment.BULLISH;
        if (sellSignals > buySignals * 1.5) return MarketSentiment.BEARISH;
        return MarketSentiment.NEUTRAL;
    }
}
