package com.bigdata.demo.service;

import com.bigdata.demo.model.AnalyticsResult;
import com.bigdata.demo.model.ProcessingResult;
import com.bigdata.demo.model.Transaction;
import com.bigdata.demo.repository.TransactionRepository;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service demonstrating Java Collections Framework optimizations for Big Data
 */
@Service
public class CollectionsOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectionsOptimizationService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    // Cache for frequently accessed data - using ConcurrentHashMap for thread safety
    private final Map<String, List<Transaction>> customerTransactionCache = new ConcurrentHashMap<>();
    
    // In-memory cache for demo purposes
    private final List<Transaction> transactions = new ArrayList<>();
    
    /**
     * Demonstrates ArrayList vs LinkedList performance comparison
     */
    public Map<String, Long> compareListPerformance(int size) {
        logger.info("Comparing ArrayList vs LinkedList performance for {} elements", size);
        
        Map<String, Long> results = new HashMap<>();
        
        // ArrayList performance test
        long startTime = System.nanoTime();
        List<Integer> arrayList = new ArrayList<>(size); // Pre-allocate capacity
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        // Random access test
        for (int i = 0; i < 1000; i++) {
            int randomIndex = (int) (Math.random() * size);
            arrayList.get(randomIndex);
        }
        long arrayListTime = System.nanoTime() - startTime;
        results.put("ArrayList_nanoseconds", arrayListTime);
        
        // LinkedList performance test
        startTime = System.nanoTime();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        // Sequential access test (LinkedList is optimized for this)
        Iterator<Integer> iterator = linkedList.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        long linkedListTime = System.nanoTime() - startTime;
        results.put("LinkedList_nanoseconds", linkedListTime);
        
        // Primitive collections test (memory efficient)
        startTime = System.nanoTime();
        TIntArrayList primitiveList = new TIntArrayList(size);
        for (int i = 0; i < size; i++) {
            primitiveList.add(i);
        }
        long primitiveTime = System.nanoTime() - startTime;
        results.put("PrimitiveList_nanoseconds", primitiveTime);
        
        logger.info("Performance comparison completed: {}", results);
        return results;
    }
    
    /**
     * Demonstrates HashMap vs TreeMap performance and use cases
     */
    public Map<String, Object> compareMapPerformance(List<Transaction> transactions) {
        logger.info("Comparing HashMap vs TreeMap performance with {} transactions", transactions.size());
        
        Map<String, Object> results = new HashMap<>();
        
        // HashMap test - O(1) average case
        long startTime = System.nanoTime();
        Map<String, BigDecimal> customerTotalsHashMap = new HashMap<>(transactions.size() / 2);
        
        for (Transaction transaction : transactions) {
            customerTotalsHashMap.merge(transaction.getCustomerId(), 
                                     transaction.getAmount(), 
                                     BigDecimal::add);
        }
        
        // Random lookups
        String[] customerIds = customerTotalsHashMap.keySet().toArray(new String[0]);
        for (int i = 0; i < 1000; i++) {
            String randomCustomer = customerIds[(int) (Math.random() * customerIds.length)];
            customerTotalsHashMap.get(randomCustomer);
        }
        
        long hashMapTime = System.nanoTime() - startTime;
        results.put("HashMap_nanoseconds", hashMapTime);
        results.put("HashMap_size", customerTotalsHashMap.size());
        
        // TreeMap test - O(log n) but sorted
        startTime = System.nanoTime();
        Map<String, BigDecimal> customerTotalsTreeMap = new TreeMap<>();
        
        for (Transaction transaction : transactions) {
            customerTotalsTreeMap.merge(transaction.getCustomerId(), 
                                      transaction.getAmount(), 
                                      BigDecimal::add);
        }
        
        // Get sorted data (TreeMap advantage)
        List<Map.Entry<String, BigDecimal>> sortedEntries = new ArrayList<>(customerTotalsTreeMap.entrySet());
        
        long treeMapTime = System.nanoTime() - startTime;
        results.put("TreeMap_nanoseconds", treeMapTime);
        results.put("TreeMap_sorted_data", sortedEntries.subList(0, Math.min(5, sortedEntries.size())));
        
        // Primitive collections for counting (memory efficient)
        startTime = System.nanoTime();
        TObjectIntHashMap<String> categoryCount = new TObjectIntHashMap<>();
        
        for (Transaction transaction : transactions) {
            categoryCount.adjustOrPutValue(transaction.getCategory(), 1, 1);
        }
        
        long primitiveMapTime = System.nanoTime() - startTime;
        results.put("PrimitiveMap_nanoseconds", primitiveMapTime);
        results.put("PrimitiveMap_categories", categoryCount.size());
        
        return results;
    }
    
    /**
     * Demonstrates Set operations for unique data handling
     */
    public Map<String, Object> demonstrateSetOperations(List<Transaction> transactions) {
        logger.info("Demonstrating Set operations with {} transactions", transactions.size());
        
        Map<String, Object> results = new HashMap<>();
        
        // HashSet for fast unique operations
        Set<String> uniqueCustomers = new HashSet<>();
        Set<String> uniqueCategories = new HashSet<>();
        Set<String> uniqueMerchants = new HashSet<>();
        
        for (Transaction transaction : transactions) {
            uniqueCustomers.add(transaction.getCustomerId());
            uniqueCategories.add(transaction.getCategory());
            uniqueMerchants.add(transaction.getMerchantId());
        }
        
        results.put("unique_customers", uniqueCustomers.size());
        results.put("unique_categories", uniqueCategories.size());
        results.put("unique_merchants", uniqueMerchants.size());
        
        // TreeSet for sorted unique values
        Set<String> sortedCategories = new TreeSet<>(uniqueCategories);
        results.put("sorted_categories", new ArrayList<>(sortedCategories));
        
        // Set operations
        Set<String> highValueCustomers = transactions.stream()
            .filter(t -> t.getAmount().compareTo(new BigDecimal("1000")) > 0)
            .map(Transaction::getCustomerId)
            .collect(Collectors.toSet());
        
        Set<String> frequentCustomers = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getCustomerId, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        // Intersection - customers who are both high-value and frequent
        Set<String> premiumCustomers = new HashSet<>(highValueCustomers);
        premiumCustomers.retainAll(frequentCustomers);
        
        results.put("high_value_customers", highValueCustomers.size());
        results.put("frequent_customers", frequentCustomers.size());
        results.put("premium_customers", premiumCustomers.size());
        results.put("premium_customer_list", new ArrayList<>(premiumCustomers));
        
        return results;
    }
    
    /**
     * Demonstrates caching strategies for frequently accessed data
     */
    public List<Transaction> getCustomerTransactionsWithCaching(String customerId) {
        // Check cache first
        if (customerTransactionCache.containsKey(customerId)) {
            logger.debug("Cache hit for customer: {}", customerId);
            return customerTransactionCache.get(customerId);
        }
        
        // Cache miss - fetch from database
        logger.debug("Cache miss for customer: {}, fetching from database", customerId);
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        
        // Store in cache with size limit
        if (customerTransactionCache.size() < 1000) { // Simple cache size limit
            customerTransactionCache.put(customerId, transactions);
        }
        
        return transactions;
    }
    
    /**
     * Demonstrates memory-efficient data structures
     */
    public Map<String, Object> demonstrateMemoryEfficiency(int dataSize) {
        logger.info("Demonstrating memory efficiency with {} elements", dataSize);
        
        Map<String, Object> results = new HashMap<>();
        
        // Memory measurement utility
        Runtime runtime = Runtime.getRuntime();
        
        // Traditional Integer list (boxing overhead)
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        
        List<Integer> integerList = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            integerList.add(i); // Boxing: int -> Integer
        }
        
        long memAfterInteger = runtime.totalMemory() - runtime.freeMemory();
        results.put("Integer_list_memory_bytes", memAfterInteger - memBefore);
        
        // Primitive int list (no boxing)
        runtime.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();
        
        TIntArrayList primitiveList = new TIntArrayList(dataSize);
        for (int i = 0; i < dataSize; i++) {
            primitiveList.add(i); // No boxing
        }
        
        long memAfterPrimitive = runtime.totalMemory() - runtime.freeMemory();
        results.put("Primitive_list_memory_bytes", memAfterPrimitive - memBefore);
        
        // Calculate savings
        long savings = (memAfterInteger - memBefore) - (memAfterPrimitive - memBefore);
        results.put("Memory_savings_bytes", savings);
        results.put("Memory_savings_percentage", 
                   savings * 100.0 / (memAfterInteger - memBefore));
        
        return results;
    }
    
    /**
     * Clears cache - useful for memory management
     */
    public void clearCache() {
        customerTransactionCache.clear();
        logger.info("Transaction cache cleared");
    }
    
    /**
     * Gets cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cache_size", customerTransactionCache.size());
        stats.put("cached_customers", new ArrayList<>(customerTransactionCache.keySet()));
        
        int totalCachedTransactions = customerTransactionCache.values().stream()
            .mapToInt(List::size)
            .sum();
        stats.put("total_cached_transactions", totalCachedTransactions);
        
        return stats;
    }
    
    /**
     * Compare ArrayList vs LinkedList performance for Big Data
     */
    public ProcessingResult compareCollectionsPerformance(int dataSize) {
        // ArrayList test
        long startTime = System.currentTimeMillis();
        List<Integer> arrayList = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            arrayList.add(i);
        }
        // Random access test
        for (int i = 0; i < 1000; i++) {
            arrayList.get(dataSize / 2);
        }
        long arrayListTime = System.currentTimeMillis() - startTime;
        
        // LinkedList test
        startTime = System.currentTimeMillis();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < dataSize; i++) {
            linkedList.add(i);
        }
        // Sequential access test
        for (Integer value : linkedList) {
            // Just iterate
        }
        long linkedListTime = System.currentTimeMillis() - startTime;
        
        String result = String.format(
            "ArrayList: %d ms (optimized for random access), LinkedList: %d ms (optimized for sequential access)",
            arrayListTime, linkedListTime
        );
        return ProcessingResult.success(result, Math.min(arrayListTime, linkedListTime));
    }
    
    /**
     * Compare HashMap vs TreeMap performance
     */
    public ProcessingResult compareMapPerformance(int dataSize) {
        // HashMap test
        long startTime = System.currentTimeMillis();
        Map<Integer, String> hashMap = new HashMap<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            hashMap.put(i, "value" + i);
        }
        // Lookup test
        for (int i = 0; i < 1000; i++) {
            hashMap.get(dataSize / 2);
        }
        long hashMapTime = System.currentTimeMillis() - startTime;
        
        // TreeMap test
        startTime = System.currentTimeMillis();
        Map<Integer, String> treeMap = new TreeMap<>();
        for (int i = 0; i < dataSize; i++) {
            treeMap.put(i, "value" + i);
        }
        // Lookup test
        for (int i = 0; i < 1000; i++) {
            treeMap.get(dataSize / 2);
        }
        long treeMapTime = System.currentTimeMillis() - startTime;
        
        String result = String.format(
            "HashMap: %d ms (O(1) lookup), TreeMap: %d ms (O(log n) lookup, sorted)",
            hashMapTime, treeMapTime
        );
        return ProcessingResult.success(result, Math.min(hashMapTime, treeMapTime));
    }
    
    /**
     * Demonstrate primitive collections for memory efficiency
     */
    public ProcessingResult demonstratePrimitiveCollections(int dataSize) {
        // Regular Integer list (boxing)
        long startTime = System.currentTimeMillis();
        List<Integer> integerList = new ArrayList<>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            integerList.add(i); // Boxing int to Integer
        }
        long integerTime = System.currentTimeMillis() - startTime;
        
        // Primitive int array (no boxing)
        startTime = System.currentTimeMillis();
        TIntArrayList primitiveList = new TIntArrayList(dataSize);
        for (int i = 0; i < dataSize; i++) {
            primitiveList.add(i); // No boxing
        }
        long primitiveTime = System.currentTimeMillis() - startTime;
        
        String result = String.format(
            "Integer List: %d ms (with boxing), Primitive List: %d ms (no boxing), Memory saved: ~%d%%",
            integerTime, primitiveTime, ((integerTime - primitiveTime) * 100 / integerTime)
        );
        return ProcessingResult.success(result, Math.min(integerTime, primitiveTime));
    }
    
    /**
     * Demonstrate thread-safe collections
     */
    public ProcessingResult demonstrateThreadSafeCollections(int dataSize) {
        // ConcurrentHashMap test
        long startTime = System.currentTimeMillis();
        Map<Integer, String> concurrentMap = new ConcurrentHashMap<>();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int thread = 0; thread < 4; thread++) {
            final int threadId = thread;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = threadId * dataSize / 4; i < (threadId + 1) * dataSize / 4; i++) {
                    concurrentMap.put(i, "thread" + threadId + "-value" + i);
                }
            }));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long concurrentTime = System.currentTimeMillis() - startTime;
        
        String result = String.format(
            "ConcurrentHashMap: %d ms with 4 threads, final size: %d",
            concurrentTime, concurrentMap.size()
        );
        return ProcessingResult.success(result, concurrentTime);
    }
    
    /**
     * Perform analytics on cached data
     */
    public AnalyticsResult performAnalytics() {
        if (transactions.isEmpty()) {
            return new AnalyticsResult.Builder()
                .category("No Data")
                .transactionCount(0)
                .build();
        }
        
        // Use the cached transactions for analytics
        BigDecimal total = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal average = total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal min = transactions.stream()
            .map(Transaction::getAmount)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal max = transactions.stream()
            .map(Transaction::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        return new AnalyticsResult.Builder()
            .category("All Transactions")
            .transactionCount(transactions.size())
            .totalAmount(total)
            .averageAmount(average)
            .minAmount(min)
            .maxAmount(max)
            .build();
    }
    
    /**
     * Load sample data for testing
     */
    public ProcessingResult loadSampleData(int recordCount) {
        long startTime = System.currentTimeMillis();
        
        transactions.clear();
        String[] categories = {"FOOD", "TRAVEL", "SHOPPING", "UTILITIES", "ENTERTAINMENT"};
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "CAD"};
        Random random = new Random();
        
        for (int i = 0; i < recordCount; i++) {
            Transaction transaction = new Transaction();
            transaction.setCustomerId("CUST" + (i % 1000));
            transaction.setAmount(BigDecimal.valueOf(random.nextDouble() * 1000 + 10));
            transaction.setCategory(categories[random.nextInt(categories.length)]);
            transaction.setCurrency(currencies[random.nextInt(currencies.length)]);
            transaction.setDescription("Transaction " + i);
            transaction.setTransactionDate(LocalDateTime.now().minusDays(random.nextInt(30)));
            transactions.add(transaction);
        }
        
        long endTime = System.currentTimeMillis();
        
        // Cache in different collections for performance testing
        updateTransactionCache();
        
        return ProcessingResult.success(
            String.format("Loaded %d sample transactions in %d ms", recordCount, endTime - startTime),
            endTime - startTime
        );
    }
    
    /**
     * Update transaction cache for performance testing
     */
    private void updateTransactionCache() {
        customerTransactionCache.clear();
        
        // Group transactions by customer ID for fast lookup
        Map<String, List<Transaction>> groupedTransactions = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getCustomerId));
        
        customerTransactionCache.putAll(groupedTransactions);
        
        logger.info("Updated transaction cache with {} customer groups", customerTransactionCache.size());
    }
    
    // ...existing methods...
}
