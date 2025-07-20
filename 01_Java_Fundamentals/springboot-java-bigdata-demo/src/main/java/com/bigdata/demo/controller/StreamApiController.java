package com.bigdata.demo.controller;

import com.bigdata.demo.model.ProcessingResult;
import com.bigdata.demo.service.StreamApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller demonstrating Java Stream API for Big Data
 * Maps to Section 3 of java_for_bigdata.txt
 */
@RestController
@RequestMapping("/api/streams")
public class StreamApiController {

    @Autowired
    private StreamApiService streamApiService;

    @GetMapping("/basic-operations")
    public ProcessingResult demonstrateBasicStreams(@RequestParam(defaultValue = "100000") int dataSize) {
        return streamApiService.demonstrateBasicStreams(dataSize);
    }

    @GetMapping("/sequential-vs-parallel")
    public ProcessingResult compareSequentialVsParallel(@RequestParam(defaultValue = "1000000") int dataSize) {
        return streamApiService.compareSequentialVsParallel(dataSize);
    }

    @GetMapping("/advanced-operations")
    public ProcessingResult demonstrateAdvancedStreams(@RequestParam(defaultValue = "50000") int dataSize) {
        return streamApiService.demonstrateAdvancedStreams(dataSize);
    }

    @GetMapping("/reduction-operations")
    public ProcessingResult demonstrateReductionOperations(@RequestParam(defaultValue = "75000") int dataSize) {
        return streamApiService.demonstrateReductionOperations(dataSize);
    }

    @GetMapping("/custom-collectors")
    public ProcessingResult demonstrateCustomCollectors(@RequestParam(defaultValue = "60000") int dataSize) {
        return streamApiService.demonstrateCustomCollectors(dataSize);
    }

    @GetMapping("/memory-efficient")
    public ProcessingResult demonstrateMemoryEfficientStreams(@RequestParam(defaultValue = "500000") int dataSize) {
        return streamApiService.demonstrateMemoryEfficientStreams(dataSize);
    }
}
