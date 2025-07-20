#!/bin/bash

# Apache Spark Big Data Platform - Startup Script
# This script starts the complete Spark platform with all dependencies

set -e

echo "🚀 Starting Apache Spark Big Data Platform..."
echo "================================================"

# Function to check if a service is ready
check_service() {
    local service=$1
    local port=$2
    local host=${3:-localhost}
    
    echo "⏳ Waiting for $service to be ready..."
    for i in {1..30}; do
        if nc -z $host $port 2>/dev/null; then
            echo "✅ $service is ready!"
            return 0
        fi
        echo "   Attempt $i/30: $service not ready yet..."
        sleep 10
    done
    echo "❌ $service failed to start within timeout"
    return 1
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Start infrastructure services
echo "🐳 Starting infrastructure services..."
docker-compose up -d zookeeper kafka postgres redis prometheus grafana

# Wait for Kafka to be ready
check_service "Kafka" 9092

# Wait for PostgreSQL to be ready
check_service "PostgreSQL" 5432

echo "📊 Starting Spark cluster..."
docker-compose up -d spark-master spark-worker-1 spark-worker-2 spark-history

# Wait for Spark Master to be ready
check_service "Spark Master" 7077

echo "📈 Starting monitoring services..."
check_service "Prometheus" 9090
check_service "Grafana" 3000

echo "📓 Starting Jupyter Notebook..."
docker-compose up -d jupyter

# Build the application if not already built
if [ ! -f "target/spark-bigdata-platform-1.0.0.jar" ]; then
    echo "🔨 Building application..."
    mvn clean package -DskipTests
fi

# Start the Spring Boot application
echo "🌟 Starting Spring Boot application..."
echo "   The application will be available at: http://localhost:8090/spark-platform"
echo "   Swagger UI will be available at: http://localhost:8090/spark-platform/swagger-ui.html"
echo ""
echo "📊 Monitoring URLs:"
echo "   Spark Master UI: http://localhost:8080"
echo "   Spark History Server: http://localhost:18080"
echo "   Grafana Dashboard: http://localhost:3000 (admin/admin)"
echo "   Prometheus: http://localhost:9090"
echo "   Jupyter Notebook: http://localhost:8888"
echo ""
echo "🔧 Useful commands:"
echo "   Check application health: curl http://localhost:8090/spark-platform/api/spark/health"
echo "   Stop all services: docker-compose down"
echo "   View logs: docker-compose logs -f [service_name]"
echo ""

# Start the application with the docker profile
if [ "$1" = "--local" ]; then
    echo "🏠 Starting with local Spark..."
    mvn spring-boot:run -Dspring.profiles.active=development
else
    echo "🐳 Starting with Docker Spark cluster..."
    mvn spring-boot:run -Dspring.profiles.active=docker
fi
