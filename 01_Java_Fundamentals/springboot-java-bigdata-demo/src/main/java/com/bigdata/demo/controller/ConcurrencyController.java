package com.bigdata.demo.controller;

import com.bigdata.demo.model.ProcessingResult;
import com.bigdata.demo.service.ConcurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller demonstrating Multithreading and Concurrency for Big Data
 * Maps to Section 2 of java_for_bigdata.txt
 */
@RestController
@RequestMapping("/api/concurrency")
public class ConcurrencyController {

    @Autowired
    private ConcurrencyService concurrencyService;

    @GetMapping("/executor-service")
    public ProcessingResult demonstrateExecutorService(@RequestParam(defaultValue = "100000") int dataSize) {
        return concurrencyService.demonstrateExecutorService(dataSize);
    }

    @GetMapping("/fork-join-pool")
    public ProcessingResult demonstrateForkJoinPool(@RequestParam(defaultValue = "100000") int dataSize) {
        return concurrencyService.demonstrateForkJoinPool(dataSize);
    }

    @GetMapping("/completable-future")
    public ProcessingResult demonstrateCompletableFuture(@RequestParam(defaultValue = "90000") int dataSize) {
        return concurrencyService.demonstrateCompletableFuture(dataSize);
    }

    @GetMapping("/parallel-streams")
    public ProcessingResult compareParallelStreams(@RequestParam(defaultValue = "1000000") int dataSize) {
        return concurrencyService.compareParallelStreams(dataSize);
    }

    @PostMapping("/async-processing")
    public CompletableFuture<ProcessingResult> startAsyncProcessing(@RequestParam(defaultValue = "50000") int dataSize) {
        return concurrencyService.asyncDataProcessing(dataSize);
    }
}
