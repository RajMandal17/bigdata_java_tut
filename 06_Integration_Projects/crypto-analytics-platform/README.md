# Crypto Analytics Platform

A comprehensive real-time cryptocurrency analytics platform built with Java Spring Boot, integrating with Gitbitex exchange for advanced trading analytics, fraud detection, and risk management.

## 🚀 Features

### Core Functionality
- **Real-time Market Data Ingestion** - Live cryptocurrency price feeds and trading data
- **Advanced Trading Signals** - Technical analysis with 15+ indicators (RSI, MACD, Bollinger Bands, etc.)
- **Risk Management** - Portfolio risk assessment, VaR calculation, stress testing
- **Fraud Detection** - ML-based fraud detection and user behavior analysis
- **Real-time Analytics** - Spark-powered analytics and metrics computation
- **WebSocket Streaming** - Live data streams for trading signals and market updates
- **RESTful APIs** - Comprehensive REST endpoints for all platform features

### Technical Stack
- **Backend**: Java 17, Spring Boot 3.2.1, Spring WebFlux
- **Big Data**: Apache Kafka, Apache Spark, Redis, MongoDB, InfluxDB
- **Machine Learning**: Smile ML, Apache Spark MLlib
- **Real-time**: WebSocket, STOMP, SockJS
- **API Integration**: OpenFeign for Gitbitex API
- **Blockchain**: Web3j for blockchain integration
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Testing**: JUnit 5, Spring Boot Test

## 📁 Project Structure

```
crypto-analytics-platform/
├── src/main/java/com/bigdata/crypto/
│   ├── CryptoAnalyticsPlatformApplication.java     # Main application class
│   ├── config/                                     # Configuration classes
│   │   ├── KafkaConfiguration.java                 # Kafka setup
│   │   ├── SparkConfiguration.java                 # Spark configuration
│   │   └── WebSocketConfiguration.java             # WebSocket setup
│   ├── controller/                                 # REST controllers
│   │   └── CryptoAnalyticsController.java          # Main API controller
│   ├── dashboard/                                  # Dashboard components
│   │   ├── CryptoDashboardController.java          # Dashboard endpoints
│   │   └── DashboardModels.java                    # Dashboard DTOs
│   ├── dto/                                        # Data Transfer Objects
│   │   ├── BulkMarketDataRequest.java              # Bulk data requests
│   │   ├── MarketDataResponse.java                 # Market data response
│   │   ├── OrderEventRequest.java                  # Order event DTO
│   │   └── TradeEventRequest.java                  # Trade event DTO
│   ├── fraud/                                      # Fraud detection system
│   │   ├── CryptoFraudDetectionService.java        # Main fraud service
│   │   ├── CryptoMLModelService.java               # ML model service
│   │   └── FraudModels.java                        # Fraud detection models
│   ├── gitbitex/                                   # Gitbitex integration
│   │   └── GitbitexApiClient.java                  # Feign client for API
│   ├── model/                                      # Domain models
│   │   ├── MarketData.java                         # Market data model
│   │   ├── OrderBook.java                          # Order book model
│   │   ├── OrderEvent.java                         # Order event model
│   │   └── TradeEvent.java                         # Trade event model
│   ├── risk/                                       # Risk management
│   │   ├── CryptoRiskManagementService.java        # Risk management service
│   │   └── RiskModels.java                         # Risk assessment models
│   ├── service/                                    # Business services
│   │   ├── AlertService.java                       # Alert notifications
│   │   ├── CryptoEventIngestionService.java        # Event ingestion
│   │   ├── OrderBookSnapshotService.java           # Order book management
│   │   └── RealTimeMetricsService.java             # Metrics computation
│   ├── signals/                                    # Trading signals
│   │   ├── CryptoTradingSignalsService.java        # Signal generation
│   │   └── SignalModels.java                       # Signal DTOs
│   ├── spark/                                      # Spark analytics
│   │   └── CryptoAnalyticsProcessor.java           # Spark data processing
│   ├── util/                                       # Utilities
│   │   └── ValidationResult.java                   # Validation helper
│   └── websocket/                                  # WebSocket components
│       ├── CryptoWebSocketController.java          # WebSocket controller
│       └── WebSocketModels.java                    # WebSocket DTOs
├── src/main/resources/
│   └── application.yml                             # Application configuration
├── src/test/java/
│   └── CryptoAnalyticsPlatformIntegrationTest.java # Integration tests
├── docker-compose.yml                              # Docker stack
└── pom.xml                                         # Maven dependencies
```

## 🔧 Setup and Installation

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Git

### 1. Clone the Repository
```bash
git clone <repository-url>
cd crypto-analytics-platform
```

### 2. Start Infrastructure Services
```bash
# Start Kafka, Redis, MongoDB, InfluxDB, and Spark
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 3. Build and Run the Application
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verify Installation
```bash
# Check application health
curl http://localhost:8080/api/v1/crypto/health

# Get API version
curl http://localhost:8080/api/v1/crypto/version
```

## 📡 API Endpoints

### Market Data
- `GET /api/v1/crypto/market/{symbol}` - Get market data for symbol
- `POST /api/v1/crypto/market/bulk` - Get bulk market data
- `GET /api/v1/crypto/metrics` - Get platform metrics

### Trading Signals
- `GET /api/v1/crypto/signals/{symbol}` - Generate trading signal
- `POST /api/v1/crypto/signals/bulk` - Generate bulk signals
- `GET /api/v1/crypto/portfolio/{userId}/recommendations` - Portfolio recommendations

### Risk Management
- `GET /api/v1/crypto/risk/portfolio/{portfolioId}` - Portfolio risk assessment
- `POST /api/v1/crypto/risk/market` - Market risk analysis
- `POST /api/v1/crypto/risk/stress-test/{portfolioId}` - Stress testing

### Fraud Detection
- `POST /api/v1/crypto/fraud/analyze` - Analyze transaction for fraud
- `GET /api/v1/crypto/behavior/{userId}` - User behavior analysis

### System
- `GET /api/v1/crypto/health` - System health status
- `GET /api/v1/crypto/version` - API version information

## 🌐 WebSocket Endpoints

### Real-time Data Streams
- `/ws/crypto` - Main WebSocket endpoint
- `/topic/market/{symbol}` - Market data updates
- `/topic/signals/{symbol}` - Trading signal updates
- `/topic/portfolio/{userId}` - Portfolio metrics
- `/topic/orderbook/{symbol}` - Order book updates
- `/topic/analytics` - Platform analytics

### Example WebSocket Usage
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Subscribe to market data
stompClient.subscribe('/topic/market/BTC-USD', function(message) {
    const marketData = JSON.parse(message.body);
    console.log('Market Update:', marketData);
});

// Subscribe to trading signals
stompClient.subscribe('/topic/signals/BTC-USD', function(message) {
    const signal = JSON.parse(message.body);
    console.log('Trading Signal:', signal);
});
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest=CryptoAnalyticsPlatformIntegrationTest
```

### Test Coverage
```bash
mvn jacoco:report
```

## 🔍 Key Components

### 1. Event Ingestion Service
- Processes real-time trading events from Gitbitex
- Validates and enriches incoming data
- Publishes events to Kafka topics
- Maintains order book snapshots

### 2. Trading Signals Service
- Implements 15+ technical indicators
- Generates buy/sell/hold signals
- Provides confidence scores and reasoning
- Supports bulk signal generation

### 3. Risk Management Service
- Calculates portfolio Value at Risk (VaR)
- Performs stress testing scenarios
- Monitors concentration risk
- Assesses market risk and correlations

### 4. Fraud Detection Service
- ML-based transaction analysis
- User behavior profiling
- Real-time fraud scoring
- Alert generation for suspicious activities

### 5. Real-time Analytics
- Spark-powered data processing
- Real-time metrics computation
- Performance monitoring
- Market sentiment analysis

## 📊 Technical Indicators

The platform implements comprehensive technical analysis:

- **Trend Indicators**: SMA, EMA, Moving Average Convergence Divergence (MACD)
- **Momentum Indicators**: Relative Strength Index (RSI), Stochastic Oscillator
- **Volatility Indicators**: Bollinger Bands, Average True Range
- **Volume Indicators**: Volume SMA, Volume Ratio
- **Support/Resistance**: Pivot Points, Fibonacci Retracements

## 🔄 Data Flow

1. **Data Ingestion**: Market data flows from Gitbitex → Kafka topics
2. **Processing**: Spark processes streaming data for analytics
3. **Storage**: Processed data stored in MongoDB/InfluxDB
4. **Caching**: Frequently accessed data cached in Redis
5. **Real-time**: WebSocket streams updates to connected clients
6. **APIs**: REST endpoints provide on-demand data access

## 🚀 Deployment

### Docker Deployment
```bash
# Build application image
docker build -t crypto-analytics-platform .

# Deploy full stack
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Verify deployment
kubectl get pods -l app=crypto-analytics
```

## 📈 Monitoring and Observability

- **Metrics**: Exposed via `/actuator/metrics`
- **Health Checks**: Available at `/actuator/health`
- **Logging**: Structured logging with correlation IDs
- **Distributed Tracing**: Integration with Zipkin/Jaeger
- **Custom Metrics**: Business metrics for trading performance

## 🔐 Security Features

- **API Authentication**: JWT-based authentication
- **Rate Limiting**: Request throttling and DoS protection
- **Input Validation**: Comprehensive request validation
- **Fraud Detection**: Real-time fraud monitoring
- **Audit Logging**: Complete audit trail for all operations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Email: support@crypto-analytics-platform.com
- Documentation: [Wiki](https://github.com/your-repo/wiki)

## 🙏 Acknowledgments

- [Gitbitex](https://github.com/gitbitex/gitbitex) for the cryptocurrency exchange integration
- Apache Foundation for Kafka and Spark
- Spring Framework team for excellent documentation
- The cryptocurrency community for market data insights

---

**Built with ❤️ for the cryptocurrency trading community**
