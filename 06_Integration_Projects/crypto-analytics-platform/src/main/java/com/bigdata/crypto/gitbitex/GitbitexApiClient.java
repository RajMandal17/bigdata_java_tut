package com.bigdata.crypto.gitbitex;

import com.bigdata.crypto.model.MarketData;
import com.bigdata.crypto.model.OrderBook;
import com.bigdata.crypto.model.TradingPair;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for Gitbitex API Integration
 * 
 * Provides access to:
 * - Market data and price information
 * - Order book depth data
 * - Trading pairs and symbols
 * - Exchange statistics
 */
@FeignClient(
    name = "gitbitex-api",
    url = "${gitbitex.api.base-url}",
    configuration = GitbitexClientConfiguration.class
)
public interface GitbitexApiClient {

    /**
     * Get all available trading pairs
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products")
    List<TradingPair> getTradingPairs();

    /**
     * Get market data for a specific symbol
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products/{symbol}/ticker")
    MarketData getMarketData(@PathVariable("symbol") String symbol);

    /**
     * Get order book for a specific symbol
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products/{symbol}/book")
    OrderBook getOrderBook(
        @PathVariable("symbol") String symbol,
        @RequestParam(value = "level", defaultValue = "2") int level
    );

    /**
     * Get recent trades for a symbol
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products/{symbol}/trades")
    List<Map<String, Object>> getRecentTrades(
        @PathVariable("symbol") String symbol,
        @RequestParam(value = "limit", defaultValue = "100") int limit
    );

    /**
     * Get candlestick data
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products/{symbol}/candles")
    List<Object[]> getCandlestickData(
        @PathVariable("symbol") String symbol,
        @RequestParam("start") String start,
        @RequestParam("end") String end,
        @RequestParam("granularity") int granularity
    );

    /**
     * Get 24h trading statistics
     */
    @GetMapping("${gitbitex.api.public-endpoint}/products/stats")
    Map<String, Object> getTradingStats();

    /**
     * Admin endpoint to create new trading pair
     */
    @PutMapping("${gitbitex.api.admin-endpoint}/products")
    Map<String, Object> createTradingPair(@RequestBody Map<String, String> request);

    /**
     * Get exchange server time
     */
    @GetMapping("${gitbitex.api.public-endpoint}/time")
    Map<String, Object> getServerTime();
}
