/*
 * StreamGroupingDemo.java
 *
 * Demonstrates grouping data using Java Stream API (similar to SQL GROUP BY).
 *
 * Key Concepts:
 *   - Collectors.groupingBy() allows grouping elements by a classifier function.
 *   - Useful for aggregating and organizing data in big data processing.
 *
 * Example: Grouping strings by their first character.
 */

import java.util.*;
import java.util.stream.*;

public class StreamGroupingDemo {
    public static void main(String[] args) {
        List<String> words = Arrays.asList("apple", "banana", "apricot", "blueberry", "avocado");

        // Group words by their first letter
        Map<Character, List<String>> grouped = words.stream()
            .collect(Collectors.groupingBy(word -> word.charAt(0)));

        System.out.println("Grouped words: " + grouped);
    }
}
