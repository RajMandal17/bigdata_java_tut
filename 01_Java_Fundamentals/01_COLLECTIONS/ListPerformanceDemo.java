/*
 * ListPerformanceDemo.java
 *
 * Purpose:
 *   Demonstrates the performance difference between ArrayList and LinkedList in Java,
 *   especially for large datasets (big data context).
 *
 * Key Concepts:
 *   - ArrayList: Backed by a dynamic array, provides O(1) random access, O(n) insert/delete in middle.
 *   - LinkedList: Doubly-linked list, O(n) random access, O(1) insert/delete at ends, O(n) in middle.
 *
 * Usage/Future:
 *   - Use ArrayList for frequent random access and when memory efficiency is important.
 *   - Use LinkedList for frequent insertions/deletions at the ends, but avoid for random access in big data.
 *   - For concurrent access, consider CopyOnWriteArrayList or Collections.synchronizedList.
 *
 * Comparison:
 *   - ArrayList is much faster for random access than LinkedList.
 *   - LinkedList can be better for queue-like operations but is rarely used in big data due to poor cache locality.
 *
 * See also:
 *   - SetDemo.java for unique data handling.
 *   - MapDemo.java for key-value operations.
 *   - ThreadSafeCollectionsDemo.java for concurrency.
 */

import java.util.*;

public class ListPerformanceDemo {
    public static void main(String[] args) {
        int dataSize = 1_000_000;
        List<Integer> arrayList = new ArrayList<>(dataSize);
        List<Integer> linkedList = new LinkedList<>();

        // Populate both lists
        for (int i = 0; i < dataSize; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // Access performance
        long start = System.nanoTime();
        arrayList.get(dataSize / 2);
        long arrayListTime = System.nanoTime() - start;

        start = System.nanoTime();
        linkedList.get(dataSize / 2);
        long linkedListTime = System.nanoTime() - start;

        System.out.println("ArrayList get(): " + arrayListTime + " ns");
        System.out.println("LinkedList get(): " + linkedListTime + " ns");
    }
}
//javac ListPerformanceDemo.java && java ListPerformanceDemo