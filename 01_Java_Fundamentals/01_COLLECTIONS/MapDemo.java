/*
 * MapDemo.java
 *
 * Purpose:
 *   Demonstrates the difference between HashMap and TreeMap for key-value operations in Java.
 *
 * Key Concepts:
 *   - HashMap: Backed by a hash table, provides O(1) put/get/remove (on average), no order.
 *   - TreeMap: Backed by a Red-Black tree, provides O(log n) put/get/remove, maintains keys in sorted order.
 *
 * Usage/Future:
 *   - Use HashMap for fast, unordered key-value storage (e.g., lookup tables in big data).
 *   - Use TreeMap when you need sorted keys (e.g., range queries, ordered analytics).
 *   - For concurrency, consider ConcurrentHashMap or Collections.synchronizedMap.
 *
 * Comparison:
 *   - HashMap is faster for most operations but does not maintain order.
 *   - TreeMap is slower but keeps keys sorted, useful for ordered data processing.
 *
 * See also:
 *   - ListPerformanceDemo.java for list performance.
 *   - SetDemo.java for unique data handling.
 *   - ThreadSafeCollectionsDemo.java for concurrency.
 */

import java.util.*;

public class MapDemo {
    public static void main(String[] args) {
        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> treeMap = new TreeMap<>();

        hashMap.put("apple", 10);
        hashMap.put("banana", 20);

        treeMap.put("apple", 10);
        treeMap.put("banana", 20);

        System.out.println("HashMap: " + hashMap);
        System.out.println("TreeMap (sorted): " + treeMap);
    }
}