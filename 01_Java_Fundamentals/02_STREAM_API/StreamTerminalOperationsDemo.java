/*
 * StreamTerminalOperationsDemo.java
 *
 * Demonstrates terminal operations in Java Stream API.
 * Shows how to use forEach, count, min, max, and reduce.
 *
 * Key Concepts:
 *   - Terminal operations produce a result or side-effect and close the stream.
 *   - Common terminal operations: forEach, count, min, max, reduce, collect.
 *
 * Example: Counting, finding min/max, and reducing a list of numbers.
 */

import java.util.*;
import java.util.stream.*;

public class StreamTerminalOperationsDemo {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(5, 3, 8, 2, 7);

        // Count elements
        long count = numbers.stream().count();
        System.out.println("Count: " + count);

        // Find minimum and maximum
        int min = numbers.stream().min(Integer::compareTo).orElse(-1);
        int max = numbers.stream().max(Integer::compareTo).orElse(-1);
        System.out.println("Min: " + min + ", Max: " + max);

        // Reduce (sum)
        int sum = numbers.stream().reduce(0, Integer::sum);
        System.out.println("Sum: " + sum);
    }
}
