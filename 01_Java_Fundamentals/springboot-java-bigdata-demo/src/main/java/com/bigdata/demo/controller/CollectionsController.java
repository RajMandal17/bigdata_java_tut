package com.bigdata.demo.controller;

import com.bigdata.demo.model.AnalyticsResult;
import com.bigdata.demo.model.ProcessingResult;
import com.bigdata.demo.service.CollectionsOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating Java Collections for Big Data
 * Maps to Section 1 of java_for_bigdata.txt
 */
@RestController
@RequestMapping("/api/collections")
public class CollectionsController {

    @Autowired
    private CollectionsOptimizationService collectionsService;

    @GetMapping("/performance-comparison")
    public ProcessingResult compareCollectionsPerformance(@RequestParam(defaultValue = "100000") int dataSize) {
        return collectionsService.compareCollectionsPerformance(dataSize);
    }

    @GetMapping("/map-performance")
    public ProcessingResult compareMapPerformance(@RequestParam(defaultValue = "50000") int dataSize) {
        return collectionsService.compareMapPerformance(dataSize);
    }

    @GetMapping("/primitive-collections")
    public ProcessingResult demonstratePrimitiveCollections(@RequestParam(defaultValue = "1000000") int dataSize) {
        return collectionsService.demonstratePrimitiveCollections(dataSize);
    }

    @GetMapping("/thread-safe-collections")
    public ProcessingResult demonstrateThreadSafeCollections(@RequestParam(defaultValue = "10000") int dataSize) {
        return collectionsService.demonstrateThreadSafeCollections(dataSize);
    }

    @GetMapping("/analytics")
    public AnalyticsResult performAnalytics() {
        return collectionsService.performAnalytics();
    }

    @PostMapping("/load-sample-data")
    public ProcessingResult loadSampleData(@RequestParam(defaultValue = "10000") int recordCount) {
        return collectionsService.loadSampleData(recordCount);
    }
}
