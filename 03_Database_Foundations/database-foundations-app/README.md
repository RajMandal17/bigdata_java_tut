# Database Foundations for Big Data

A comprehensive project demonstrating database foundations concepts for Big Data applications using MySQL, MongoDB, Redis, Kafka, Spark, Elasticsearch, and Kibana with Docker.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Services](#services)
- [Database Schemas](#database-schemas)
- [Usage Examples](#usage-examples)
- [Performance Optimization](#performance-optimization)
- [Monitoring](#monitoring)
- [Curriculum Mapping](#curriculum-mapping)
- [Exercises](#exercises)
- [Assessment Checklist](#assessment-checklist)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This project implements a production-grade database foundations system that demonstrates:

- **Polyglot Persistence**: Using SQL (MySQL) and NoSQL (MongoDB) databases
- **Real-time Streaming**: Kafka for event streaming and processing
- **Caching Strategy**: Redis for high-performance caching
- **Big Data Processing**: Apache Spark for analytics
- **Search & Analytics**: Elasticsearch and Kibana for search and visualization
- **Spring Boot Integration**: Complete application with repositories, services, and REST APIs

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │────│  Spring Boot    │────│     MySQL       │
│                 │    │   Application   │    │  (Transactions) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │                       │
                               │               ┌─────────────────┐
                               │───────────────│    MongoDB      │
                               │               │    (Events)     │
                               │               └─────────────────┘
                               │
                       ┌───────┴───────┐
                       │               │
              ┌─────────────────┐ ┌─────────────────┐
              │     Redis       │ │     Kafka       │
              │   (Caching)     │ │  (Streaming)    │
              └─────────────────┘ └─────────────────┘
                                          │
                 ┌────────────────────────┼────────────────────────┐
                 │                       │                        │
        ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
        │     Spark       │    │ Elasticsearch   │    │    Kibana       │
        │  (Analytics)    │    │   (Search)      │    │ (Visualization) │
        └─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Maven 3.6+
- 8GB+ RAM recommended
- 20GB+ free disk space

## 🚀 Quick Start

### 1. Clone and Navigate

```bash
cd /home/raj/Desktop/Bigdata_Java_learning/03_Database_Foundations/database-foundations-app
```

### 2. Start All Services

```bash
# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 3. Wait for Services to Initialize

Services will take 2-3 minutes to fully start. Monitor logs:

```bash
# Check specific service logs
docker-compose logs -f mysql
docker-compose logs -f mongodb
docker-compose logs -f kafka
```

### 4. Verify Services

- **MySQL**: `mysql -h localhost -P 3306 -u bigdata_user -p`
- **MongoDB**: `mongosh mongodb://bigdata_user:bigdata_password@localhost:27017/bigdata_foundations`
- **Kafka UI**: http://localhost:8080
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Redis**: `redis-cli -h localhost -p 6379`

### 5. Run the Spring Boot Application

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will be available at http://localhost:8090

## 🗄️ Services

### MySQL (Port 3306)
- **Purpose**: ACID-compliant transactions, structured data
- **Data**: Users, Orders, Products, Transactions
- **Features**: Partitioning, indexing, stored procedures

### MongoDB (Port 27017)
- **Purpose**: Document storage, real-time events
- **Data**: User events, activity logs, session data
- **Features**: Aggregation pipelines, geospatial queries

### Redis (Port 6379)
- **Purpose**: High-performance caching
- **Data**: User sessions, analytics cache, rate limiting
- **Features**: TTL, pub/sub, data structures

### Kafka (Port 9092/9093)
- **Purpose**: Event streaming and messaging
- **Topics**: user-events, order-events, system-metrics
- **Features**: Partitioning, consumer groups, retention

### Spark (Port 8081)
- **Purpose**: Big data processing and analytics
- **Data**: Batch processing, ETL, ML pipelines
- **Features**: Distributed computing, in-memory processing

### Elasticsearch (Port 9200)
- **Purpose**: Search, analytics, logging
- **Data**: Product catalog, user activity, system logs
- **Features**: Full-text search, aggregations

### Kibana (Port 5601)
- **Purpose**: Data visualization and monitoring
- **Features**: Dashboards, logs analysis, metrics

## 🗃️ Database Schemas

### MySQL Schema

**Users Table:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATETIME,
    city VARCHAR(100),
    country VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Orders Table (Partitioned):**
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED') DEFAULT 'PENDING',
    order_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, order_date),
    FOREIGN KEY (user_id) REFERENCES users(id)
) PARTITION BY RANGE (YEAR(order_date)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026)
);
```

### MongoDB Schema

**Events Collection:**
```javascript
{
  "_id": ObjectId("..."),
  "eventId": "evt_123456789",
  "userId": "user_001",
  "eventType": "purchase",
  "timestamp": ISODate("2024-01-20T10:30:00Z"),
  "data": {
    "productId": "prod_456",
    "amount": 99.99,
    "category": "electronics"
  },
  "metadata": {
    "source": "web_app",
    "sessionId": "sess_789",
    "userAgent": "Mozilla/5.0..."
  },
  "processed": false,
  "tags": ["premium", "electronics"]
}
```

## 📱 Usage Examples

### 1. REST API Endpoints

```bash
# Get user analytics
curl http://localhost:8090/api/users/123/analytics

# Create user with activity
curl -X POST http://localhost:8090/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@example.com",
    "country": "USA"
  }'

# Get recent user activity
curl http://localhost:8090/api/users/123/activity?days=7

# Dashboard data
curl http://localhost:8090/api/dashboard?startDate=2024-01-01&endDate=2024-01-31
```

### 2. Kafka Integration

```bash
# Send user event
curl -X POST http://localhost:8090/api/events/user \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123",
    "action": "login",
    "data": {"ip": "192.168.1.100"}
  }'

# Send order event  
curl -X POST http://localhost:8090/api/events/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD001",
    "userId": "123", 
    "status": "confirmed",
    "amount": 299.99
  }'
```

### 3. Database Queries

**MySQL Analytics:**
```sql
-- Daily revenue by country
SELECT 
    DATE(o.created_at) as order_date,
    u.country,
    SUM(o.total_amount) as daily_revenue,
    COUNT(o.id) as order_count
FROM orders o
JOIN users u ON o.user_id = u.id
WHERE o.created_at >= '2024-01-01'
GROUP BY DATE(o.created_at), u.country
ORDER BY order_date DESC, daily_revenue DESC;
```

**MongoDB Aggregation:**
```javascript
// User activity summary
db.events.aggregate([
  {
    $match: {
      "timestamp": {
        $gte: ISODate("2024-01-01"),
        $lte: ISODate("2024-01-31")
      }
    }
  },
  {
    $group: {
      _id: "$userId",
      totalEvents: { $sum: 1 },
      eventTypes: { $addToSet: "$eventType" },
      lastActivity: { $max: "$timestamp" }
    }
  },
  {
    $sort: { totalEvents: -1 }
  }
]);
```

### 4. Spark Analytics

```bash
# Run Spark analytics job
docker exec -it spark-master spark-submit \
  --class "com.bigdata.foundations.spark.AnalyticsJob" \
  /opt/spark/sample-data/process_data.py
```

## ⚡ Performance Optimization

### MySQL Optimization

**Configuration (my.cnf):**
```ini
[mysqld]
innodb_buffer_pool_size = 4G
innodb_log_file_size = 1G
max_connections = 500
query_cache_size = 0
innodb_flush_log_at_trx_commit = 2
```

**Indexing Strategy:**
```sql
-- Composite indexes for common queries
CREATE INDEX idx_user_country_status ON users (country, status);
CREATE INDEX idx_order_user_date ON orders (user_id, order_date);
CREATE INDEX idx_order_status_date ON orders (status, order_date);

-- Covering indexes for analytics
CREATE INDEX idx_analytics_covering ON orders (order_date, status, total_amount);
```

### MongoDB Optimization

**Indexes:**
```javascript
// Compound indexes
db.events.createIndex({ "userId": 1, "timestamp": -1 });
db.events.createIndex({ "eventType": 1, "processed": 1 });

// Text search
db.events.createIndex({ "data.productName": "text" });

// TTL for automatic cleanup
db.events.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 7776000 });
```

### Redis Optimization

**Configuration:**
```properties
# Cache TTL settings
spring.cache.redis.time-to-live=1800000
spring.cache.redis.cache-null-values=false

# Connection pool
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=8
```

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8090/actuator/health

# Service health
curl http://localhost:9200/_cluster/health
curl http://localhost:27017/admin/
```

### Metrics

- **Application**: http://localhost:8090/actuator/metrics
- **Elasticsearch**: http://localhost:9200/_stats
- **Kafka UI**: http://localhost:8080

### Kibana Dashboards

1. Open http://localhost:5601
2. Go to "Discover" to explore logs
3. Create visualizations for:
   - User activity trends
   - System performance metrics
   - Error rates and patterns

## 📚 Curriculum Mapping

| Topic | Implementation | Location |
|-------|---------------|----------|
| SQL vs NoSQL | MySQL + MongoDB | `mysql/` + `mongodb/` |
| ACID Compliance | MySQL transactions | `TransactionService.java` |
| Document Storage | MongoDB events | `Event.java` |
| Indexing Strategies | Both databases | `schema.sql` + `init.js` |
| Query Optimization | Native queries | `*Repository.java` |
| Caching | Redis integration | `@Cacheable` annotations |
| Event Streaming | Kafka topics | `EventStreamProcessor.java` |
| Big Data Processing | Spark jobs | `spark/init/` |
| Search & Analytics | Elasticsearch | `elasticsearch/init/` |
| Polyglot Persistence | Multiple databases | `DataIntegrationService.java` |

## 🎯 Exercises

### Exercise 1: Database Design
Design and implement:
1. Additional tables for a complete e-commerce system
2. Proper foreign key relationships
3. Appropriate indexes for performance

### Exercise 2: Advanced Queries
Implement:
1. Complex analytics queries with JOINs
2. MongoDB aggregation pipelines
3. Performance comparison between SQL and NoSQL

### Exercise 3: Real-time Processing
Build:
1. Kafka producer for real-time events
2. Stream processing with multiple consumers
3. Event correlation across databases

### Exercise 4: Performance Testing
Execute:
1. Load testing with 1M+ records
2. Query performance analysis
3. Optimization recommendations

### Exercise 5: Data Migration
Create:
1. ETL pipeline between databases
2. Data validation and integrity checks
3. Rollback mechanisms

## ✅ Assessment Checklist

- [ ] **Database Selection**: Can justify when to use SQL vs NoSQL
- [ ] **Schema Design**: Can design efficient, normalized schemas
- [ ] **Indexing**: Understands and implements proper indexing strategies
- [ ] **Query Optimization**: Can write and optimize complex queries
- [ ] **Transactions**: Understands ACID properties and transactions
- [ ] **Aggregations**: Can build complex aggregation pipelines
- [ ] **Caching**: Implements effective caching strategies
- [ ] **Event Streaming**: Can design and implement event-driven architectures
- [ ] **Performance**: Can identify and resolve performance bottlenecks
- [ ] **Integration**: Can integrate multiple database technologies
- [ ] **Monitoring**: Can set up monitoring and alerting
- [ ] **Best Practices**: Follows database best practices and conventions

## 🔧 Troubleshooting

### Common Issues

**1. Services not starting:**
```bash
# Check Docker resources
docker system df
docker system prune

# Restart services
docker-compose down
docker-compose up -d
```

**2. Connection errors:**
```bash
# Check service logs
docker-compose logs [service-name]

# Verify network connectivity
docker network ls
docker network inspect database-foundations-app_bigdata-network
```

**3. Performance issues:**
```bash
# Monitor resource usage
docker stats

# Check database performance
SHOW PROCESSLIST; -- MySQL
db.currentOp(); -- MongoDB
```

**4. Data issues:**
```bash
# Reset data
docker-compose down -v
docker-compose up -d

# Check data initialization
docker-compose logs mysql
docker-compose logs mongodb
```

### Memory Requirements

- **Minimum**: 8GB RAM
- **Recommended**: 16GB RAM
- **Production**: 32GB+ RAM

### Port Conflicts

If ports are in use, modify `docker-compose.yml`:
```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # Change external port
```

## 📝 Next Steps

After completing this module:

1. **Apache Kafka Deep Dive** (Module 04)
2. **Apache Spark for Big Data** (Module 05)
3. **Microservices Architecture** (Module 06)
4. **Cloud Deployment** (Module 07)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Happy Learning!** 🚀

For questions or support, please create an issue in the repository.
