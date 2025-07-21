/*
 * StreamBasicsDemo.java
 *
 * Demonstrates basic usage of Java Stream API for data processing.
 * Shows how to use intermediate and terminal operations on collections.
 *
 * Key Concepts:
 *   - Streams provide a functional approach to processing data in Java.
 *   - Intermediate operations: filter, map, sorted, distinct, etc.
 *   - Terminal operations: collect, forEach, count, min, max, reduce, etc.
 *
 * Example: Filtering, mapping, and collecting data from a list.
 */

import java.util.*;
import java.util.stream.*;

public class StreamBasicsDemo1 {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Alice");
 

        // Filter names starting with 'A', convert to uppercase, remove duplicates, and collect to list
        List<String> result = names.stream()
            .filter(name -> name.startsWith("A"))
            .map(String::toUpperCase)
            .distinct()
            .collect(Collectors.toList());

        System.out.println("Filtered and mapped names: " + result);


        List<String> resultName = names.stream()
        .filter(name -> name.startsWith("B"))
        .collect(Collectors.toList());

         System.out.println("Filtered and mapped names: " + resultName);

    }
}
