/*
 * ParallelStreamDemo.java
 *
 * Demonstrates parallel streams in Java for faster data processing on large datasets.
 *
 * Key Concepts:
 *   - parallelStream() enables parallel processing using multiple CPU cores.
 *   - Useful for CPU-intensive operations on large collections.
 *   - Not recommended for I/O-bound tasks.
 *
 * Example: Calculating average using parallelStream.
 */

import java.util.*;
import java.util.stream.*;

public class ParallelStreamDemo {
    public static void main(String[] args) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }

        // Calculate average using parallel stream
        double average = numbers.parallelStream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        System.out.println("Average: " + average);
    }
}
