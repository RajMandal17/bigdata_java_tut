/*
 * StreamPerformanceTips.java
 *
 * Tips and best practices for using Java Stream API efficiently in big data scenarios.
 *
 * Key Concepts:
 *   - Use parallel streams for CPU-intensive operations on large datasets.
 *   - Avoid parallel streams for I/O-bound tasks.
 *   - Use primitive streams (IntStream, DoubleStream) for better performance.
 *   - Combine operations to reduce intermediate collections.
 *
 * Example: Using IntStream for efficient processing.
 */

import java.util.stream.*;

public class StreamPerformanceTips {
    public static void main(String[] args) {
        // Using IntStream to avoid boxing/unboxing
        int sum = IntStream.range(0, 1_000_000).sum();
        System.out.println("Sum using IntStream: " + sum);
    }
}
