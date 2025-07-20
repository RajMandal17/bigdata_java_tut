package com.bigdata.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Main controller providing information about all available Big Data demos
 * Maps to ALL sections of java_for_bigdata.txt
 */
@RestController
public class BigDataDemoController {

    @GetMapping("/")
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Java Big Data Fundamentals Demo");
        info.put("description", "Spring Boot project demonstrating all Java concepts from java_for_bigdata.txt");
        
        Map<String, Object> endpoints = new HashMap<>();
        
        // Collections Framework Demo (Section 1)
        Map<String, String> collections = new HashMap<>();
        collections.put("performance-comparison", "GET /api/collections/performance-comparison?dataSize=100000");
        collections.put("map-performance", "GET /api/collections/map-performance?dataSize=50000");
        collections.put("primitive-collections", "GET /api/collections/primitive-collections?dataSize=1000000");
        collections.put("thread-safe-collections", "GET /api/collections/thread-safe-collections?dataSize=10000");
        collections.put("analytics", "GET /api/collections/analytics");
        collections.put("load-sample-data", "POST /api/collections/load-sample-data?recordCount=10000");
        endpoints.put("1_Collections_Framework", collections);
        
        // Stream API Demo (Section 2)
        Map<String, String> streams = new HashMap<>();
        streams.put("basic-operations", "GET /api/streams/basic-operations?dataSize=100000");
        streams.put("sequential-vs-parallel", "GET /api/streams/sequential-vs-parallel?dataSize=1000000");
        streams.put("advanced-operations", "GET /api/streams/advanced-operations?dataSize=50000");
        streams.put("reduction-operations", "GET /api/streams/reduction-operations?dataSize=75000");
        streams.put("custom-collectors", "GET /api/streams/custom-collectors?dataSize=60000");
        streams.put("memory-efficient", "GET /api/streams/memory-efficient?dataSize=500000");
        endpoints.put("2_Stream_API", streams);
        
        // Multithreading and Concurrency Demo (Section 3)
        Map<String, String> concurrency = new HashMap<>();
        concurrency.put("executor-service", "GET /api/concurrency/executor-service?dataSize=100000");
        concurrency.put("fork-join-pool", "GET /api/concurrency/fork-join-pool?dataSize=100000");
        concurrency.put("completable-future", "GET /api/concurrency/completable-future?dataSize=90000");
        concurrency.put("parallel-streams", "GET /api/concurrency/parallel-streams?dataSize=1000000");
        concurrency.put("async-processing", "POST /api/concurrency/async-processing?dataSize=50000");
        endpoints.put("3_Multithreading_Concurrency", concurrency);
        
        info.put("endpoints", endpoints);
        
        Map<String, String> quickStart = new HashMap<>();
        quickStart.put("1", "Load sample data: POST /api/collections/load-sample-data");
        quickStart.put("2", "Test collections: GET /api/collections/performance-comparison");
        quickStart.put("3", "Test streams: GET /api/streams/sequential-vs-parallel");
        quickStart.put("4", "Test concurrency: GET /api/concurrency/executor-service");
        quickStart.put("5", "Run analytics: GET /api/collections/analytics");
        info.put("quickStart", quickStart);
        
        Map<String, String> concepts = new HashMap<>();
        concepts.put("Collections Framework", "ArrayList vs LinkedList, HashMap vs TreeMap, Primitive Collections, Thread-safe Collections");
        concepts.put("Stream API", "Filter, Map, Reduce, Parallel Processing, Custom Collectors, Memory Efficiency");
        concepts.put("Concurrency", "ExecutorService, ForkJoinPool, CompletableFuture, Parallel Streams, Async Processing");
        concepts.put("Memory Management", "JVM Tuning, Primitive Collections, Object Pooling, Garbage Collection");
        concepts.put("Exception Handling", "Try-with-resources, Custom Exceptions, Circuit Breakers");
        info.put("javaConcepts", concepts);
        
        return info;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Java Big Data Fundamentals Demo is running");
        return status;
    }
}
