/*
 * SetDemo.java
 *
 * Purpose:
 *   Demonstrates the difference between HashSet and TreeSet for handling unique data in Java.
 *
 * Key Concepts:
 *   - HashSet: Backed by a hash table, provides O(1) add, remove, and contains (on average), no order.
 *   - TreeSet: Backed by a Red-Black tree, provides O(log n) add, remove, and contains, maintains sorted order.
 *
 * Usage/Future:
 *   - Use HashSet for fast, unordered unique data storage (e.g., deduplication in big data).
 *   - Use TreeSet when you need unique data in sorted order (e.g., top-N queries, range queries).
 *   - For concurrency, consider ConcurrentSkipListSet or Collections.synchronizedSet.
 *
 * Comparison:
 *   - HashSet is faster for most operations but does not maintain order.
 *   - TreeSet is slower but keeps elements sorted, useful for ordered analytics.
 *
 * See also:
 *   - ListPerformanceDemo.java for list performance.
 *   - MapDemo.java for key-value operations.
 *   - ThreadSafeCollectionsDemo.java for concurrency.
 */

import java.util.*;

public class SetDemo {
    public static void main(String[] args) {
        Set<String> hashSet = new HashSet<>();
        Set<String> treeSet = new TreeSet<>();

        hashSet.add("delta");
        hashSet.add("alpha");
        hashSet.add("charlie");

        treeSet.add("delta");
        treeSet.add("alpha");
        treeSet.add("charlie");

        System.out.println("HashSet: " + hashSet);
        System.out.println("TreeSet (sorted): " + treeSet);
    }
}