package com.bigdata.demo.service;

import com.bigdata.demo.model.ProcessingResult;
import com.bigdata.demo.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service demonstrating Java Stream API for Big Data
 * Maps to Section 3 of java_for_bigdata.txt
 */
@Service
public class StreamApiService {

    /**
     * Demonstrate basic stream operations on large dataset
     */
    public ProcessingResult demonstrateBasicStreams(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        List<Integer> numbers = IntStream.range(0, dataSize)
            .boxed()
            .collect(Collectors.toList());
        
        // Chain multiple stream operations
        List<Integer> result = numbers.stream()
            .filter(n -> n % 2 == 0)           // Filter even numbers
            .map(n -> n * n)                   // Square them
            .filter(n -> n > 100)              // Filter squares > 100
            .sorted()                          // Sort them
            .limit(1000)                       // Take first 1000
            .collect(Collectors.toList());
        
        long endTime = System.currentTimeMillis();
        return ProcessingResult.success(
            String.format("Basic streams: Processed %d numbers, filtered to %d results in %d ms", 
                         dataSize, result.size(), endTime - startTime),
            endTime - startTime
        );
    }

    /**
     * Demonstrate parallel streams with performance comparison
     */
    public ProcessingResult compareSequentialVsParallel(int dataSize) {
        List<Integer> numbers = IntStream.range(0, dataSize)
            .boxed()
            .collect(Collectors.toList());
        
        // Sequential processing
        long startTime = System.currentTimeMillis();
        long sequentialSum = numbers.stream()
            .filter(n -> n % 2 == 0)
            .mapToLong(n -> (long) n * n)
            .sum();
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        // Parallel processing
        startTime = System.currentTimeMillis();
        long parallelSum = numbers.parallelStream()
            .filter(n -> n % 2 == 0)
            .mapToLong(n -> (long) n * n)
            .sum();
        long parallelTime = System.currentTimeMillis() - startTime;
        
        double speedup = (double) sequentialTime / parallelTime;
        
        return ProcessingResult.success(
            String.format("Sequential: %d ms (sum: %d), Parallel: %d ms (sum: %d), Speedup: %.2fx", 
                         sequentialTime, sequentialSum, parallelTime, parallelSum, speedup),
            Math.min(sequentialTime, parallelTime)
        );
    }

    /**
     * Demonstrate advanced stream operations (grouping, partitioning, etc.)
     */
    public ProcessingResult demonstrateAdvancedStreams(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        // Generate sample transactions
        List<Transaction> transactions = generateSampleTransactions(dataSize);
        
        // Group by currency
        Map<String, List<Transaction>> groupedByCurrency = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getCurrency));
        
        // Partition by large amounts
        Map<Boolean, List<Transaction>> partitionedByAmount = transactions.stream()
            .collect(Collectors.partitioningBy(t -> t.getAmount().compareTo(BigDecimal.valueOf(1000.0)) > 0));
        
        // Calculate statistics
        DoubleSummaryStatistics stats = transactions.stream()
            .mapToDouble(t -> t.getAmount().doubleValue())
            .summaryStatistics();
        
        // Find top 10 largest transactions
        List<Transaction> topTransactions = transactions.stream()
            .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
            .limit(10)
            .collect(Collectors.toList());
        
        long endTime = System.currentTimeMillis();
        
        String summary = String.format(
            "Advanced streams on %d transactions: %d currencies, %d large transactions (>1000), " +
            "avg amount: %.2f, processing time: %d ms",
            dataSize, groupedByCurrency.size(), partitionedByAmount.get(true).size(),
            stats.getAverage(), endTime - startTime
        );
        
        return ProcessingResult.success(summary, endTime - startTime);
    }

    /**
     * Demonstrate stream reduction operations
     */
    public ProcessingResult demonstrateReductionOperations(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        List<Transaction> transactions = generateSampleTransactions(dataSize);
        
        // Various reduction operations
        double totalAmount = transactions.stream()
            .mapToDouble(t -> t.getAmount().doubleValue())
            .sum();
        
        Optional<Transaction> maxTransaction = transactions.stream()
            .max(Comparator.comparing(Transaction::getAmount));
        
        Optional<Transaction> minTransaction = transactions.stream()
            .min(Comparator.comparing(Transaction::getAmount));
        
        long uniqueCurrencies = transactions.stream()
            .map(Transaction::getCurrency)
            .distinct()
            .count();
        
        // Custom reduction
        String concatenatedIds = transactions.stream()
            .limit(10)
            .map(Transaction::getCustomerId)
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
        
        long endTime = System.currentTimeMillis();
        
        String summary = String.format(
            "Reduction operations: Total amount: %.2f, Max: %.2f, Min: %.2f, " +
            "Unique currencies: %d, Sample IDs: %s, Time: %d ms",
            totalAmount, 
            maxTransaction.map(t -> t.getAmount().doubleValue()).orElse(0.0),
            minTransaction.map(t -> t.getAmount().doubleValue()).orElse(0.0),
            uniqueCurrencies, concatenatedIds, endTime - startTime
        );
        
        return ProcessingResult.success(summary, endTime - startTime);
    }

    /**
     * Demonstrate custom collectors
     */
    public ProcessingResult demonstrateCustomCollectors(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        List<Transaction> transactions = generateSampleTransactions(dataSize);
        
        // Custom collector to group by currency and calculate total amount
        Map<String, Double> currencyTotals = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCurrency,
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));
        
        // Custom collector for statistical summary by currency
        Map<String, DoubleSummaryStatistics> currencyStats = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCurrency,
                Collectors.summarizingDouble(t -> t.getAmount().doubleValue())
            ));
        
        // Joining operation
        String currenciesList = transactions.stream()
            .map(Transaction::getCurrency)
            .distinct()
            .sorted()
            .collect(Collectors.joining(", ", "[", "]"));
        
        long endTime = System.currentTimeMillis();
        
        String summary = String.format(
            "Custom collectors: %d currencies processed, total combinations: %d, " +
            "currencies: %s, processing time: %d ms",
            currencyTotals.size(), currencyStats.size(), currenciesList, endTime - startTime
        );
        
        return ProcessingResult.success(summary, endTime - startTime);
    }

    /**
     * Demonstrate memory-efficient stream processing
     */
    public ProcessingResult demonstrateMemoryEfficientStreams(int dataSize) {
        long startTime = System.currentTimeMillis();
        
        // Process large dataset without loading everything into memory
        OptionalDouble averageAmount = Stream.iterate(1, n -> n + 1)
            .limit(dataSize)
            .map(this::createTransaction)
            .filter(t -> t.getAmount().compareTo(BigDecimal.valueOf(100.0)) > 0)
            .mapToDouble(t -> t.getAmount().doubleValue())
            .average();
        
        // Count transactions by currency without storing them
        Map<String, Long> currencyCounts = Stream.iterate(1, n -> n + 1)
            .limit(dataSize)
            .map(this::createTransaction)
            .collect(Collectors.groupingBy(
                Transaction::getCurrency,
                Collectors.counting()
            ));
        
        long endTime = System.currentTimeMillis();
        
        String summary = String.format(
            "Memory-efficient processing: %d records processed, average amount: %.2f, " +
            "currency distribution: %s, time: %d ms",
            dataSize, averageAmount.orElse(0.0), currencyCounts, endTime - startTime
        );
        
        return ProcessingResult.success(summary, endTime - startTime);
    }

    private List<Transaction> generateSampleTransactions(int count) {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD"};
        Random random = new Random();
        
        return IntStream.range(0, count)
            .mapToObj(i -> createTransaction(i))
            .collect(Collectors.toList());
    }
    
    private Transaction createTransaction(int id) {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD"};
        Random random = new Random(id); // Use id as seed for consistent results
        
        return new Transaction(
            "TXN" + id,
            BigDecimal.valueOf(random.nextDouble() * 10000), // Amount between 0 and 10000
            currencies[random.nextInt(currencies.length)],
            new Date()
        );
    }
}
