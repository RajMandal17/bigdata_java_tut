# Spring Boot Big Data Application

This is an advanced Spring Boot application that demonstrates comprehensive Big Data processing patterns, following the `springboot_for_bigdata.txt` curriculum.

## Status

✅ **Successfully Created and Compiled** - The application compiles and starts without errors  
✅ **Spring Boot Integration** - Properly configured with all required dependencies  
✅ **Database Integration** - H2 database starts, JPA entities created, sample data available  
✅ **Actuator Monitoring** - Health checks, metrics, and Prometheus endpoints active  
✅ **Scheduled Tasks** - Background processing and health monitoring running  
⚠️ **MongoDB** - Ready for integration but requires local MongoDB instance  
⚠️ **Kafka** - Configured but requires local Kafka instance for full functionality  
⚠️ **API Endpoints** - Some endpoints may need sample data generation for testing

## Features Demonstrated

### 1. Core Spring Boot Concepts
- **Auto-configuration** with custom properties
- **Dependency Injection** throughout the application layers
- **Spring Boot Starters** for web, data, and monitoring
- **Application Profiles** (dev, prod) for different environments
- **Configuration Properties** with `@ConfigurationProperties`

### 2. REST API Development
- **Complete CRUD operations** for transaction management
- **Request validation** with custom annotations
- **Pagination and sorting** for large datasets
- **Error handling** with global exception handler
- **API documentation** through code comments

### 3. Database Integration
- **JPA/Hibernate** with optimized entity mapping
- **Custom queries** using JPQL and native SQL
- **Database indexing** for performance
- **Connection pooling** with HikariCP
- **Multi-database support** (H2 for dev, MySQL for prod)

### 4. Async Processing
- **Thread pool configuration** for concurrent processing
- **@Async methods** for non-blocking operations
- **CompletableFuture** for async results
- **Batch processing** for large datasets

### 5. Monitoring & Health Checks
- **Spring Actuator** for application monitoring
- **Custom health indicators** for database connectivity
- **Prometheus metrics** for performance monitoring
- **Custom metrics** for transaction processing

### 6. Scheduled Tasks
- **@Scheduled** methods for periodic tasks
- **Cron expressions** for complex scheduling
- **Data archival** and maintenance tasks
- **Report generation** automation

### 7. Validation & Error Handling
- **Bean Validation** with JSR-303 annotations
- **Custom validators** for business rules
- **Global exception handling** with consistent error responses
- **Constraint validation** for API parameters

## Quick Start

### 1. Build and Run
```bash
cd springboot-bigdata-app
mvn clean compile
mvn spring-boot:run
```

### 2. Access Points
- **Application**: http://localhost:8082
- **H2 Console**: http://localhost:8082/h2-console
- **Health Check**: http://localhost:8082/actuator/health
- **Metrics**: http://localhost:8082/actuator/metrics
- **Prometheus**: http://localhost:8082/actuator/prometheus

### 3. Database Connection (H2 Console)
- **JDBC URL**: `jdbc:h2:mem:bigdata_db`
- **Username**: `sa`
- **Password**: `password`

## API Endpoints

### Transaction Management

#### Create Transaction
```bash
curl -X POST "http://localhost:8082/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST_12345",
    "amount": 150.75,
    "category": "SHOPPING",
    "currency": "USD",
    "merchantId": "MERCHANT_001",
    "description": "Online purchase"
  }'
```

#### Get Customer Transactions (with pagination)
```bash
curl "http://localhost:8082/api/transactions/customer/CUST_12345?page=0&size=10&sortBy=timestamp&sortDir=desc"
```

#### Batch Processing
```bash
curl -X POST "http://localhost:8082/api/transactions/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "customerId": "CUST_001",
      "amount": 100.00,
      "category": "FOOD",
      "currency": "USD"
    },
    {
      "customerId": "CUST_002", 
      "amount": 250.50,
      "category": "SHOPPING",
      "currency": "USD"
    }
  ]'
```

#### High Value Transactions
```bash
curl "http://localhost:8082/api/transactions/high-value?threshold=1000"
```

#### Transaction Analytics
```bash
curl "http://localhost:8082/api/transactions/analytics?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59"
```

#### Fraud Detection
```bash
curl "http://localhost:8082/api/transactions/suspicious/CUST_12345"
```

#### Generate Sample Data
```bash
curl -X POST "http://localhost:8082/api/transactions/generate-sample?count=1000"
```

## Data Model

### Transaction Entity
```json
{
  "id": 1,
  "customerId": "CUST_12345",
  "amount": 150.75,
  "timestamp": "2024-01-15T12:30:00",
  "category": "SHOPPING",
  "status": "APPROVED",
  "merchantId": "MERCHANT_001",
  "description": "Online purchase",
  "currency": "USD"
}
```

### Transaction Status Values
- `PENDING` - Awaiting approval
- `APPROVED` - Transaction approved
- `DECLINED` - Transaction declined
- `FAILED` - Transaction failed
- `CANCELLED` - Transaction cancelled
- `REFUNDED` - Transaction refunded

## Configuration

### Application Properties
```yaml
# Custom Big Data properties
bigdata:
  batch-size: 1000
  max-concurrent-tasks: 10
  data-directory: /tmp/bigdata
  kafka-enabled: true
```

### Profile-Specific Configuration
- **Development** (`application-dev.yml`): H2 database, debug logging
- **Production** (`application-prod.yml`): MySQL database, optimized settings

## Monitoring

### Health Checks
- **Overall Health**: `GET /actuator/health`
- **Database Health**: Included in health endpoint
- **Application Info**: `GET /actuator/info`

### Metrics
- **Transaction Processing**: Count and timing metrics
- **Batch Processing**: Batch size and success rate
- **Database Operations**: Connection pool and query metrics
- **JVM Metrics**: Memory, threads, garbage collection

### Prometheus Metrics
Access at: `GET /actuator/prometheus`

Key metrics:
- `transactions_processed_total`
- `transactions_failed_total`
- `transactions_processing_time_seconds`

## Big Data Patterns Demonstrated

### 1. **Scalable Data Processing**
- Batch processing for large datasets
- Async processing to avoid blocking
- Pagination for memory-efficient data retrieval

### 2. **Performance Optimization**
- Database indexing on frequently queried columns
- Connection pooling for database efficiency
- Thread pools for concurrent processing

### 3. **Data Analytics**
- Aggregation queries for business insights
- Time-based analytics with date ranges
- Customer behavior analysis

### 4. **Monitoring & Observability**
- Custom metrics for business KPIs
- Health checks for system reliability
- Structured logging for troubleshooting

### 5. **Production Readiness**
- Environment-specific configurations
- Error handling and validation
- Security considerations (validation, input sanitization)

## Technology Stack

- **Spring Boot 2.7+** - Core framework
- **Spring Data JPA** - Database abstraction
- **Spring Kafka** - Message streaming
- **H2 Database** - Development database
- **MySQL** - Production database
- **Micrometer** - Metrics collection
- **Jackson** - JSON processing
- **Bean Validation** - Input validation
- **Spring Actuator** - Monitoring endpoints

## Learning Objectives Covered

✅ **Spring Boot Fundamentals**
- Auto-configuration and dependency injection
- Starter dependencies and application structure

✅ **REST API Development**
- Controller design and request mapping
- Validation and error handling

✅ **Data Persistence**
- JPA entity mapping and repository patterns
- Query optimization and indexing

✅ **Async Processing**
- Thread pool configuration
- Batch processing patterns

✅ **Monitoring & Health**
- Custom health indicators
- Application metrics and monitoring

✅ **Configuration Management**
- Profile-specific properties
- Externalized configuration

## Next Steps

This application prepares you for:
- **Module 3**: Database optimization and advanced SQL
- **Module 4**: Apache Kafka integration and streaming
- **Module 5**: Apache Spark for distributed processing
- **Module 6**: Docker containerization and Kubernetes deployment

## Development Notes

### Building the Project
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Schema
The application automatically creates the database schema on startup using JPA DDL generation. Sample data is loaded from `data.sql`.

### Kafka Integration
Kafka integration is enabled by default but will gracefully degrade if Kafka is not available. Set `bigdata.kafka-enabled=false` to disable Kafka features.

This application demonstrates production-ready Spring Boot patterns for Big Data processing! 🚀
