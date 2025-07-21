package com.bigdata.crypto.service;

import com.bigdata.crypto.gitbitex.GitbitexApiClient;
import com.bigdata.crypto.model.MarketData;
import com.bigdata.crypto.model.OrderBook;
import com.bigdata.crypto.model.OrderEvent;
import com.bigdata.crypto.model.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/crypto-events")
public class CryptoEventIngestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoEventIngestionService.class);
    
    @Autowired
    private GitbitexApiClient gitbitexClient;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private OrderBookSnapshotService orderBookService;
    
    @Autowired
    private RealTimeMetricsService metricsService;
    
    /**
     * Webhook endpoint for Gitbitex trading events
     */
    @PostMapping("/webhook/trades")
    public ResponseEntity<Map<String, Object>> handleTradeEvent(
            @RequestBody @Valid TradeEventRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            logger.info("Received trade event: {}", request);
            
            // Validate webhook signature (optional security measure)
            if (!validateWebhookSignature(request, httpRequest)) {
                logger.warn("Invalid webhook signature for trade: {}", request.getTradeId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook signature"));
            }
            
            // Enrich trade event with market data
            TradeEvent tradeEvent = enrichTradeEvent(request);
            
            // Send to Kafka for real-time analytics
            CompletableFuture<Void> future = sendTradeEventToKafka(tradeEvent);
            
            // Update order book snapshot
            orderBookService.updateFromTrade(tradeEvent);
            
            // Update real-time metrics
            metricsService.updateTradeMetrics(tradeEvent);
            
            logger.info("Successfully processed trade event: {}", tradeEvent.getTradeId());
            
            return ResponseEntity.ok(Map.of(
                "status", "processed",
                "tradeId", tradeEvent.getTradeId(),
                "timestamp", tradeEvent.getTimestamp(),
                "symbol", tradeEvent.getSymbol()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing trade event: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Processing failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Webhook endpoint for Gitbitex order events
     */
    @PostMapping("/webhook/orders")
    public ResponseEntity<Map<String, Object>> handleOrderEvent(
            @RequestBody @Valid OrderEventRequest request) {
        
        try {
            logger.info("Received order event: {}", request);
            
            OrderEvent orderEvent = convertToOrderEvent(request);
            
            // Validate order event
            ValidationResult validation = validateOrderEvent(orderEvent);
            if (!validation.isValid()) {
                logger.warn("Invalid order event: {}, errors: {}", orderEvent, validation.getErrors());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid order event", "details", validation.getErrors()));
            }
            
            // Send to Kafka
            CompletableFuture<Void> future = sendOrderEventToKafka(orderEvent);
            
            // Update real-time metrics
            metricsService.updateOrderMetrics(orderEvent);
            
            logger.info("Successfully processed order event: {}", orderEvent.getOrderId());
            
            return ResponseEntity.ok(Map.of(
                "status", "accepted",
                "orderId", orderEvent.getOrderId(),
                "timestamp", orderEvent.getTimestamp()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing order event: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Processing failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Fetch market data from Gitbitex API
     */
    @GetMapping("/market-data/{symbol}")
    public ResponseEntity<MarketDataResponse> getMarketData(@PathVariable String symbol) {
        try {
            logger.info("Fetching market data for symbol: {}", symbol);
            
            MarketData marketData = gitbitexClient.getMarketData(symbol);
            OrderBook orderBook = gitbitexClient.getOrderBook(symbol, 100);
            
            MarketDataResponse response = MarketDataResponse.builder()
                .symbol(symbol)
                .price(marketData.getPrice())
                .volume(marketData.getVolume())
                .high(marketData.getHigh())
                .low(marketData.getLow())
                .change(marketData.getChange())
                .changePercent(marketData.getChangePercent())
                .orderBook(orderBook)
                .timestamp(Instant.now())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching market data for symbol: {}", symbol, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bulk fetch market data for multiple symbols
     */
    @PostMapping("/market-data/bulk")
    public ResponseEntity<Map<String, MarketDataResponse>> getBulkMarketData(
            @RequestBody BulkMarketDataRequest request) {
        
        try {
            Map<String, MarketDataResponse> responses = new java.util.HashMap<>();
            
            for (String symbol : request.getSymbols()) {
                try {
                    MarketData marketData = gitbitexClient.getMarketData(symbol);
                    OrderBook orderBook = gitbitexClient.getOrderBook(symbol, 50);
                    
                    MarketDataResponse response = MarketDataResponse.builder()
                        .symbol(symbol)
                        .price(marketData.getPrice())
                        .volume(marketData.getVolume())
                        .orderBook(orderBook)
                        .timestamp(Instant.now())
                        .build();
                    
                    responses.put(symbol, response);
                    
                } catch (Exception e) {
                    logger.warn("Failed to fetch market data for symbol: {}", symbol, e);
                    // Continue with other symbols
                }
            }
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error in bulk market data fetch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private TradeEvent enrichTradeEvent(TradeEventRequest request) {
        try {
            // Get additional market context from Gitbitex API
            MarketData marketData = gitbitexClient.getMarketData(request.getSymbol());
            
            return TradeEvent.builder()
                .tradeId(request.getTradeId())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .amount(request.getQuantity().multiply(request.getPrice()))
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
                .buyOrderId(request.getBuyOrderId())
                .sellOrderId(request.getSellOrderId())
                .userId(request.getUserId())
                .marketData(marketData)
                .metadata(Map.of(
                    "exchange", "gitbitex",
                    "version", "1.0",
                    "source", "webhook",
                    "enriched", true
                ))
                .build();
                
        } catch (Exception e) {
            logger.warn("Failed to enrich trade event, using basic data: {}", request, e);
            
            // Fallback to basic trade event without enrichment
            return TradeEvent.builder()
                .tradeId(request.getTradeId())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .amount(request.getQuantity().multiply(request.getPrice()))
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
                .buyOrderId(request.getBuyOrderId())
                .sellOrderId(request.getSellOrderId())
                .userId(request.getUserId())
                .metadata(Map.of(
                    "exchange", "gitbitex",
                    "version", "1.0",
                    "source", "webhook",
                    "enriched", false
                ))
                .build();
        }
    }
    
    private OrderEvent convertToOrderEvent(OrderEventRequest request) {
        return new OrderEvent(
            request.getOrderId(),
            request.getSymbol(),
            request.getSide(),
            request.getType(),
            request.getQuantity(),
            request.getPrice(),
            request.getUserId()
        );
    }
    
    private boolean validateWebhookSignature(TradeEventRequest request, HttpServletRequest httpRequest) {
        // Implement signature validation logic here
        // This would typically involve HMAC verification with a shared secret
        String signature = httpRequest.getHeader("X-Gitbitex-Signature");
        if (signature == null) {
            return true; // Skip validation if no signature header (for testing)
        }
        
        // TODO: Implement actual signature validation
        return true;
    }
    
    private ValidationResult validateOrderEvent(OrderEvent orderEvent) {
        ValidationResult result = new ValidationResult();
        
        if (orderEvent.getSymbol() == null || orderEvent.getSymbol().trim().isEmpty()) {
            result.addError("Symbol is required");
        }
        
        if (orderEvent.getSide() == null || 
            (!orderEvent.getSide().equals("BUY") && !orderEvent.getSide().equals("SELL"))) {
            result.addError("Side must be BUY or SELL");
        }
        
        if (orderEvent.getQuantity() == null || orderEvent.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Quantity must be positive");
        }
        
        if (orderEvent.getPrice() == null || orderEvent.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Price must be positive");
        }
        
        if (orderEvent.getUserId() == null || orderEvent.getUserId().trim().isEmpty()) {
            result.addError("User ID is required");
        }
        
        return result;
    }
    
    private CompletableFuture<Void> sendTradeEventToKafka(TradeEvent tradeEvent) {
        return CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("gitbitex-trades", tradeEvent.getSymbol(), tradeEvent);
                logger.debug("Sent trade event to Kafka: {}", tradeEvent.getTradeId());
            } catch (Exception e) {
                logger.error("Failed to send trade event to Kafka: {}", tradeEvent, e);
                throw new RuntimeException("Kafka send failed", e);
            }
        });
    }
    
    private CompletableFuture<Void> sendOrderEventToKafka(OrderEvent orderEvent) {
        return CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send("gitbitex-orders", orderEvent.getSymbol(), orderEvent);
                logger.debug("Sent order event to Kafka: {}", orderEvent.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to send order event to Kafka: {}", orderEvent, e);
                throw new RuntimeException("Kafka send failed", e);
            }
        });
    }
}
