package com.bigdata.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class BulkMarketDataRequest {
    
    @NotEmpty(message = "Symbols list cannot be empty")
    @JsonProperty("symbols")
    private List<String> symbols;
    
    @JsonProperty("includeOrderBook")
    private boolean includeOrderBook = true;
    
    @JsonProperty("orderBookDepth")
    private int orderBookDepth = 50;
    
    public BulkMarketDataRequest() {}
    
    public BulkMarketDataRequest(List<String> symbols) {
        this.symbols = symbols;
    }
    
    public BulkMarketDataRequest(List<String> symbols, boolean includeOrderBook, int orderBookDepth) {
        this.symbols = symbols;
        this.includeOrderBook = includeOrderBook;
        this.orderBookDepth = orderBookDepth;
    }
    
    // Getters and Setters
    public List<String> getSymbols() {
        return symbols;
    }
    
    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }
    
    public boolean isIncludeOrderBook() {
        return includeOrderBook;
    }
    
    public void setIncludeOrderBook(boolean includeOrderBook) {
        this.includeOrderBook = includeOrderBook;
    }
    
    public int getOrderBookDepth() {
        return orderBookDepth;
    }
    
    public void setOrderBookDepth(int orderBookDepth) {
        this.orderBookDepth = orderBookDepth;
    }
    
    @Override
    public String toString() {
        return "BulkMarketDataRequest{" +
                "symbols=" + symbols +
                ", includeOrderBook=" + includeOrderBook +
                ", orderBookDepth=" + orderBookDepth +
                '}';
    }
}
