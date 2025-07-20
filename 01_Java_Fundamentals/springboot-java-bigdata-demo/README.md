# Java Big Data Fundamentals - Spring Boot Demo

This Spring Boot project demonstrates all Java concepts from `java_for_bigdata.txt` through practical REST API endpoints.

## Quick Start

1. **Run the application:**
   ```bash
   cd springboot-java-bigdata-demo
   mvn spring-boot:run
   ```

2. **Access the API overview:**
   ```
   http://localhost:8081
   ```

3. **Load sample data:**
   ```bash
   curl -X POST "http://localhost:8081/api/collections/load-sample-data?recordCount=10000"
   ```

## API Endpoints by Concept

### 1. Collections Framework (`java_for_bigdata.txt` Section 1)
- `GET /api/collections/performance-comparison` - ArrayList vs LinkedList
- `GET /api/collections/map-performance` - HashMap vs TreeMap  
- `GET /api/collections/primitive-collections` - Memory efficiency
- `GET /api/collections/thread-safe-collections` - Concurrent collections

### 2. Stream API (`java_for_bigdata.txt` Section 2)
- `GET /api/streams/basic-operations` - Filter, map, reduce
- `GET /api/streams/sequential-vs-parallel` - Performance comparison
- `GET /api/streams/advanced-operations` - GroupBy, partitioning
- `GET /api/streams/memory-efficient` - Large dataset processing

### 3. Concurrency (`java_for_bigdata.txt` Section 3)
- `GET /api/concurrency/executor-service` - Thread pools
- `GET /api/concurrency/fork-join-pool` - Divide-and-conquer
- `GET /api/concurrency/completable-future` - Async processing
- `POST /api/concurrency/async-processing` - Spring @Async

## Example Usage

```bash
# Compare collections performance with 100K records
curl "http://localhost:8081/api/collections/performance-comparison?dataSize=100000"

# Test parallel vs sequential streams with 1M records  
curl "http://localhost:8081/api/streams/sequential-vs-parallel?dataSize=1000000"

# Test concurrent processing
curl "http://localhost:8081/api/concurrency/executor-service?dataSize=200000"
```

## What You'll Learn

- **Performance Characteristics**: See O(1) vs O(n) vs O(log n) in action
- **Memory Optimization**: Compare boxed vs primitive collections
- **Parallel Processing**: Measure speedup from concurrency
- **Stream Processing**: Functional programming for Big Data
- **Best Practices**: Production-ready patterns for large datasets

## Technology Stack

- **Spring Boot 2.7+** - Web framework and dependency injection
- **H2 Database** - In-memory database for demos
- **Trove4j** - High-performance primitive collections
- **JPA/Hibernate** - Data persistence layer
- **Maven** - Build and dependency management

## Next Steps

This project prepares you for:
- Module 2: Advanced Spring Boot features
- Module 3: Database optimization techniques  
- Module 4: Apache Kafka integration
- Module 5: Apache Spark distributed computing

Each concept demonstrated here scales to real Big Data scenarios!
