package com.bigdata.crypto.websocket;

import com.bigdata.crypto.model.MarketData;
import com.bigdata.crypto.model.TradeEvent;
import com.bigdata.crypto.signals.CryptoTradingSignalsService;
import com.bigdata.crypto.signals.SignalModels.TradingSignal;
import com.bigdata.crypto.service.RealTimeMetricsService;
import com.bigdata.crypto.websocket.WebSocketModels.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket controller for real-time cryptocurrency data streaming
 * Provides live market data, trading signals, and analytics
 */
@Controller
@Slf4j
public class CryptoWebSocketController extends TextWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CryptoTradingSignalsService tradingSignalsService;

    @Autowired
    private RealTimeMetricsService metricsService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionSubscriptions = new ConcurrentHashMap<>();

    /**
     * Handle market data subscription requests
     */
    @MessageMapping("/subscribe/market/{symbol}")
    @SendTo("/topic/market/{symbol}")
    public MarketDataUpdate subscribeToMarketData(String symbol) {
        log.info("Client subscribed to market data for symbol: {}", symbol);
        
        // Return latest market data
        return MarketDataUpdate.builder()
                .symbol(symbol)
                .price(getCurrentPrice(symbol))
                .volume(getCurrentVolume(symbol))
                .change24h(getChange24h(symbol))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Handle trading signals subscription
     */
    @MessageMapping("/subscribe/signals/{symbol}")
    @SendTo("/topic/signals/{symbol}")
    public TradingSignalUpdate subscribeToTradingSignals(String symbol) {
        log.info("Client subscribed to trading signals for symbol: {}", symbol);
        
        try {
            TradingSignal signal = tradingSignalsService.generateSignal(symbol);
            return TradingSignalUpdate.builder()
                    .symbol(symbol)
                    .signalType(signal.getSignalType().toString())
                    .strength(signal.getStrength().toString())
                    .confidence(signal.getConfidence())
                    .targetPrice(signal.getTargetPrice())
                    .stopLoss(signal.getStopLoss())
                    .reasoning(signal.getReasoning())
                    .timestamp(signal.getTimestamp())
                    .build();
        } catch (Exception e) {
            log.error("Error generating signal for {}: {}", symbol, e.getMessage());
            return TradingSignalUpdate.builder()
                    .symbol(symbol)
                    .signalType("HOLD")
                    .strength("WEAK")
                    .confidence(BigDecimal.ZERO)
                    .timestamp(Instant.now())
                    .build();
        }
    }

    /**
     * Handle portfolio metrics subscription
     */
    @MessageMapping("/subscribe/portfolio/{userId}")
    @SendTo("/topic/portfolio/{userId}")
    public PortfolioMetricsUpdate subscribeToPortfolioMetrics(String userId) {
        log.info("Client subscribed to portfolio metrics for user: {}", userId);
        
        return PortfolioMetricsUpdate.builder()
                .userId(userId)
                .totalValue(getTotalPortfolioValue(userId))
                .totalPnL(getTotalPnL(userId))
                .dailyPnL(getDailyPnL(userId))
                .riskScore(getPortfolioRiskScore(userId))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Handle order book subscription
     */
    @MessageMapping("/subscribe/orderbook/{symbol}")
    @SendTo("/topic/orderbook/{symbol}")
    public OrderBookUpdate subscribeToOrderBook(String symbol) {
        log.info("Client subscribed to order book for symbol: {}", symbol);
        
        return OrderBookUpdate.builder()
                .symbol(symbol)
                .bids(generateSampleBids(symbol))
                .asks(generateSampleAsks(symbol))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Scheduled task to broadcast live market data updates
     */
    @Scheduled(fixedRate = 1000) // Every second
    public void broadcastMarketDataUpdates() {
        List<String> popularSymbols = Arrays.asList("BTC-USD", "ETH-USD", "ADA-USD", "DOT-USD");
        
        for (String symbol : popularSymbols) {
            try {
                MarketDataUpdate update = MarketDataUpdate.builder()
                        .symbol(symbol)
                        .price(getCurrentPrice(symbol))
                        .volume(getCurrentVolume(symbol))
                        .change24h(getChange24h(symbol))
                        .timestamp(Instant.now())
                        .build();
                
                messagingTemplate.convertAndSend("/topic/market/" + symbol, update);
                
            } catch (Exception e) {
                log.error("Error broadcasting market data for {}: {}", symbol, e.getMessage());
            }
        }
    }

    /**
     * Scheduled task to broadcast trading signals
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void broadcastTradingSignals() {
        List<String> popularSymbols = Arrays.asList("BTC-USD", "ETH-USD", "ADA-USD", "DOT-USD");
        
        for (String symbol : popularSymbols) {
            try {
                TradingSignal signal = tradingSignalsService.generateSignal(symbol);
                
                TradingSignalUpdate update = TradingSignalUpdate.builder()
                        .symbol(symbol)
                        .signalType(signal.getSignalType().toString())
                        .strength(signal.getStrength().toString())
                        .confidence(signal.getConfidence())
                        .targetPrice(signal.getTargetPrice())
                        .stopLoss(signal.getStopLoss())
                        .reasoning(signal.getReasoning())
                        .timestamp(signal.getTimestamp())
                        .build();
                
                messagingTemplate.convertAndSend("/topic/signals/" + symbol, update);
                
            } catch (Exception e) {
                log.error("Error broadcasting signal for {}: {}", symbol, e.getMessage());
            }
        }
    }

    /**
     * Scheduled task to broadcast analytics updates
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void broadcastAnalyticsUpdates() {
        try {
            AnalyticsUpdate update = AnalyticsUpdate.builder()
                    .totalTradingVolume(metricsService.getTotalTradingVolume())
                    .activeTradersCount(metricsService.getActiveUsersCount())
                    .topPerformingAssets(getTopPerformingAssets())
                    .marketSentiment(getMarketSentiment())
                    .timestamp(Instant.now())
                    .build();
            
            messagingTemplate.convertAndSend("/topic/analytics", update);
            
        } catch (Exception e) {
            log.error("Error broadcasting analytics updates: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            WebSocketRequest request = objectMapper.readValue(payload, WebSocketRequest.class);
            
            handleSubscriptionRequest(session, request);
            
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
            sendErrorMessage(session, "Invalid message format");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        removeSessionSubscriptions(session);
    }

    private void handleSubscriptionRequest(WebSocketSession session, WebSocketRequest request) {
        try {
            String topic = request.getTopic();
            String symbol = request.getSymbol();
            
            // Add session to subscription
            subscriptions.computeIfAbsent(topic + ":" + symbol, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionSubscriptions.put(session.getId(), topic + ":" + symbol);
            
            // Send initial data based on subscription type
            switch (topic) {
                case "market":
                    sendMarketDataUpdate(session, symbol);
                    break;
                case "signals":
                    sendTradingSignalUpdate(session, symbol);
                    break;
                case "orderbook":
                    sendOrderBookUpdate(session, symbol);
                    break;
                default:
                    sendErrorMessage(session, "Unknown topic: " + topic);
            }
            
        } catch (Exception e) {
            log.error("Error handling subscription request: {}", e.getMessage());
            sendErrorMessage(session, "Subscription failed");
        }
    }

    private void sendMarketDataUpdate(WebSocketSession session, String symbol) throws IOException {
        MarketDataUpdate update = MarketDataUpdate.builder()
                .symbol(symbol)
                .price(getCurrentPrice(symbol))
                .volume(getCurrentVolume(symbol))
                .change24h(getChange24h(symbol))
                .timestamp(Instant.now())
                .build();
        
        String json = objectMapper.writeValueAsString(update);
        session.sendMessage(new TextMessage(json));
    }

    private void sendTradingSignalUpdate(WebSocketSession session, String symbol) throws IOException {
        try {
            TradingSignal signal = tradingSignalsService.generateSignal(symbol);
            
            TradingSignalUpdate update = TradingSignalUpdate.builder()
                    .symbol(symbol)
                    .signalType(signal.getSignalType().toString())
                    .strength(signal.getStrength().toString())
                    .confidence(signal.getConfidence())
                    .targetPrice(signal.getTargetPrice())
                    .stopLoss(signal.getStopLoss())
                    .reasoning(signal.getReasoning())
                    .timestamp(signal.getTimestamp())
                    .build();
            
            String json = objectMapper.writeValueAsString(update);
            session.sendMessage(new TextMessage(json));
            
        } catch (Exception e) {
            log.error("Error sending signal update: {}", e.getMessage());
        }
    }

    private void sendOrderBookUpdate(WebSocketSession session, String symbol) throws IOException {
        OrderBookUpdate update = OrderBookUpdate.builder()
                .symbol(symbol)
                .bids(generateSampleBids(symbol))
                .asks(generateSampleAsks(symbol))
                .timestamp(Instant.now())
                .build();
        
        String json = objectMapper.writeValueAsString(update);
        session.sendMessage(new TextMessage(json));
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            ErrorMessage errorMsg = ErrorMessage.builder()
                    .error(error)
                    .timestamp(Instant.now())
                    .build();
            
            String json = objectMapper.writeValueAsString(errorMsg);
            session.sendMessage(new TextMessage(json));
            
        } catch (Exception e) {
            log.error("Error sending error message: {}", e.getMessage());
        }
    }

    private void removeSessionSubscriptions(WebSocketSession session) {
        String subscription = sessionSubscriptions.remove(session.getId());
        if (subscription != null) {
            Set<WebSocketSession> sessions = subscriptions.get(subscription);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    subscriptions.remove(subscription);
                }
            }
        }
    }

    // Helper methods for sample data generation
    private BigDecimal getCurrentPrice(String symbol) {
        // Simulate price data - in production would come from real data source
        Random random = new Random();
        switch (symbol) {
            case "BTC-USD":
                return new BigDecimal(50000 + random.nextInt(5000));
            case "ETH-USD":
                return new BigDecimal(3000 + random.nextInt(500));
            case "ADA-USD":
                return new BigDecimal(1 + random.nextDouble());
            case "DOT-USD":
                return new BigDecimal(30 + random.nextInt(10));
            default:
                return new BigDecimal(100 + random.nextInt(50));
        }
    }

    private BigDecimal getCurrentVolume(String symbol) {
        Random random = new Random();
        return new BigDecimal(1000000 + random.nextInt(500000));
    }

    private BigDecimal getChange24h(String symbol) {
        Random random = new Random();
        return new BigDecimal((random.nextGaussian() * 5)); // ±5% change
    }

    private BigDecimal getTotalPortfolioValue(String userId) {
        Random random = new Random();
        return new BigDecimal(100000 + random.nextInt(50000));
    }

    private BigDecimal getTotalPnL(String userId) {
        Random random = new Random();
        return new BigDecimal(random.nextGaussian() * 5000);
    }

    private BigDecimal getDailyPnL(String userId) {
        Random random = new Random();
        return new BigDecimal(random.nextGaussian() * 1000);
    }

    private BigDecimal getPortfolioRiskScore(String userId) {
        Random random = new Random();
        return new BigDecimal(20 + random.nextInt(60));
    }

    private List<OrderBookEntry> generateSampleBids(String symbol) {
        List<OrderBookEntry> bids = new ArrayList<>();
        BigDecimal basePrice = getCurrentPrice(symbol);
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            BigDecimal price = basePrice.multiply(new BigDecimal(0.999 - i * 0.001));
            BigDecimal quantity = new BigDecimal(random.nextInt(1000) + 100);
            bids.add(new OrderBookEntry(price, quantity));
        }
        
        return bids;
    }

    private List<OrderBookEntry> generateSampleAsks(String symbol) {
        List<OrderBookEntry> asks = new ArrayList<>();
        BigDecimal basePrice = getCurrentPrice(symbol);
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            BigDecimal price = basePrice.multiply(new BigDecimal(1.001 + i * 0.001));
            BigDecimal quantity = new BigDecimal(random.nextInt(1000) + 100);
            asks.add(new OrderBookEntry(price, quantity));
        }
        
        return asks;
    }

    private List<String> getTopPerformingAssets() {
        return Arrays.asList("BTC-USD", "ETH-USD", "ADA-USD");
    }

    private String getMarketSentiment() {
        String[] sentiments = {"BULLISH", "BEARISH", "NEUTRAL"};
        Random random = new Random();
        return sentiments[random.nextInt(sentiments.length)];
    }
}
