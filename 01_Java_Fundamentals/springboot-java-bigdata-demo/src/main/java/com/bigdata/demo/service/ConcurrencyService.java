package com.bigdata.demo.service;

import com.bigdata.demo.model.ProcessingResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Service demonstrating Multithreading and Concurrency for Big Data
 * Maps to Section 2 of java_for_bigdata.txt
 */
@Service
public class ConcurrencyService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    /**
     * Demonstrate ExecutorService for parallel data processing
     */
    public ProcessingResult demonstrateExecutorService(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        // Simulate processing large dataset with multiple threads
        List<Future<Integer>> futures = new ArrayList<>();
        int chunkSize = dataSize / 4; // Split into 4 chunks
        
        for (int i = 0; i < 4; i++) {
            final int start = i * chunkSize;
            final int end = (i == 3) ? dataSize : (i + 1) * chunkSize;
            
            Future<Integer> future = executorService.submit(() -> {
                return processDataChunk(start, end);
            });
            futures.add(future);
        }
        
        int totalProcessed = 0;
        try {
            for (Future<Integer> future : futures) {
                totalProcessed += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            return ProcessingResult.error("ExecutorService processing failed: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        return ProcessingResult.success(
            String.format("ExecutorService: Processed %d records in %d ms using %d threads", 
                         totalProcessed, endTime - startTime, 4),
            endTime - startTime
        );
    }

    /**
     * Demonstrate ForkJoinPool with RecursiveTask
     */
    public ProcessingResult demonstrateForkJoinPool(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        DataProcessingTask task = new DataProcessingTask(0, dataSize);
        Integer result = forkJoinPool.invoke(task);
        
        long endTime = System.currentTimeMillis();
        return ProcessingResult.success(
            String.format("ForkJoinPool: Processed %d records in %d ms", 
                         result, endTime - startTime),
            endTime - startTime
        );
    }

    /**
     * Demonstrate CompletableFuture for asynchronous processing
     */
    public ProcessingResult demonstrateCompletableFuture(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        // Create multiple async tasks
        CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> 
            processDataChunk(0, dataSize / 3));
        CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> 
            processDataChunk(dataSize / 3, 2 * dataSize / 3));
        CompletableFuture<Integer> task3 = CompletableFuture.supplyAsync(() -> 
            processDataChunk(2 * dataSize / 3, dataSize));
        
        // Combine results
        CompletableFuture<Integer> combinedTask = task1
            .thenCombine(task2, Integer::sum)
            .thenCombine(task3, Integer::sum);
        
        try {
            Integer totalProcessed = combinedTask.get();
            long endTime = System.currentTimeMillis();
            return ProcessingResult.success(
                String.format("CompletableFuture: Processed %d records in %d ms", 
                             totalProcessed, endTime - startTime),
                endTime - startTime
            );
        } catch (InterruptedException | ExecutionException e) {
            return ProcessingResult.error("CompletableFuture processing failed: " + e.getMessage());
        }
    }

    /**
     * Demonstrate parallel streams vs sequential streams
     */
    public ProcessingResult compareParallelStreams(int dataSize) {
        // Sequential stream
        long startTime = System.currentTimeMillis();
        long sequentialSum = IntStream.range(0, dataSize)
            .filter(i -> i % 2 == 0)
            .map(i -> i * i)
            .sum();
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        // Parallel stream
        startTime = System.currentTimeMillis();
        long parallelSum = IntStream.range(0, dataSize)
            .parallel()
            .filter(i -> i % 2 == 0)
            .map(i -> i * i)
            .sum();
        long parallelTime = System.currentTimeMillis() - startTime;
        
        return ProcessingResult.success(
            String.format("Sequential: %d ms (sum: %d), Parallel: %d ms (sum: %d), Speedup: %.2fx", 
                         sequentialTime, sequentialSum, parallelTime, parallelSum,
                         (double) sequentialTime / parallelTime),
            Math.min(sequentialTime, parallelTime)
        );
    }

    /**
     * Simulate data processing for a chunk
     */
    private Integer processDataChunk(int start, int end) {
        try {
            // Simulate some processing time
            Thread.sleep(10);
            
            // Simulate complex calculation
            int processed = 0;
            for (int i = start; i < end; i++) {
                if (i % 2 == 0) {
                    processed++;
                }
            }
            return processed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * ForkJoinPool RecursiveTask implementation
     */
    private static class DataProcessingTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 1000;
        private final int start;
        private final int end;
        
        public DataProcessingTask(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Integer compute() {
            if (end - start <= THRESHOLD) {
                // Process directly
                int processed = 0;
                for (int i = start; i < end; i++) {
                    if (i % 2 == 0) {
                        processed++;
                    }
                }
                return processed;
            } else {
                // Split the task
                int mid = start + (end - start) / 2;
                DataProcessingTask leftTask = new DataProcessingTask(start, mid);
                DataProcessingTask rightTask = new DataProcessingTask(mid, end);
                
                leftTask.fork();
                int rightResult = rightTask.compute();
                int leftResult = leftTask.join();
                
                return leftResult + rightResult;
            }
        }
    }

    @Async
    public CompletableFuture<ProcessingResult> asyncDataProcessing(int dataSize) {
        try {
            Thread.sleep(2000); // Simulate long-running task
            ProcessingResult result = ProcessingResult.success(
                String.format("Async processing completed for %d records", dataSize),
                2000
            );
            return CompletableFuture.completedFuture(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(
                ProcessingResult.error("Async processing interrupted")
            );
        }
    }
}
