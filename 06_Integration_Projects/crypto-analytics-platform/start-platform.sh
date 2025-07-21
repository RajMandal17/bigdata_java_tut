#!/bin/bash

# Crypto Analytics Platform Startup Script
# This script starts all required services and the application

set -e

echo "🚀 Starting Crypto Analytics Platform..."
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' | head -1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    print_status "Java $JAVA_VERSION detected"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8+."
        exit 1
    fi
    print_status "Maven detected"
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose."
        exit 1
    fi
    print_status "Docker and Docker Compose detected"
}

# Start infrastructure services
start_infrastructure() {
    print_info "Starting infrastructure services (Kafka, Redis, MongoDB, InfluxDB, Spark)..."
    
    if [ -f "docker-compose.yml" ]; then
        docker-compose up -d
        
        # Wait for services to be ready
        print_info "Waiting for services to start..."
        sleep 30
        
        # Check if services are running
        if docker-compose ps | grep -q "Up"; then
            print_status "Infrastructure services started successfully"
        else
            print_error "Failed to start some infrastructure services"
            docker-compose logs
            exit 1
        fi
    else
        print_error "docker-compose.yml not found in current directory"
        exit 1
    fi
}

# Build the application
build_application() {
    print_info "Building the application..."
    
    if [ -f "pom.xml" ]; then
        mvn clean compile -q
        if [ $? -eq 0 ]; then
            print_status "Application built successfully"
        else
            print_error "Failed to build the application"
            exit 1
        fi
    else
        print_error "pom.xml not found. Make sure you're in the project root directory."
        exit 1
    fi
}

# Start the application
start_application() {
    print_info "Starting Crypto Analytics Platform..."
    
    # Set application properties
    export SPRING_PROFILES_ACTIVE=dev
    export JAVA_OPTS="-Xmx2g -Xms1g"
    
    # Start the application in background
    nohup mvn spring-boot:run > application.log 2>&1 &
    APP_PID=$!
    
    # Wait for application to start
    print_info "Waiting for application to start..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/api/v1/crypto/health > /dev/null 2>&1; then
            print_status "Application started successfully (PID: $APP_PID)"
            echo $APP_PID > app.pid
            break
        fi
        
        if [ $i -eq 60 ]; then
            print_error "Application failed to start within 60 seconds"
            print_info "Check application.log for details"
            kill $APP_PID 2>/dev/null || true
            exit 1
        fi
        
        sleep 1
    done
}

# Display startup information
show_startup_info() {
    echo ""
    echo "🎉 Crypto Analytics Platform is now running!"
    echo "==========================================="
    echo ""
    echo "📊 Application URLs:"
    echo "   Health Check: http://localhost:8080/api/v1/crypto/health"
    echo "   API Documentation: http://localhost:8080/swagger-ui.html"
    echo "   WebSocket Endpoint: ws://localhost:8080/ws"
    echo ""
    echo "🔧 Infrastructure Services:"
    echo "   Kafka: localhost:9092"
    echo "   Redis: localhost:6379"
    echo "   MongoDB: localhost:27017"
    echo "   InfluxDB: localhost:8086"
    echo "   Spark UI: http://localhost:4040"
    echo ""
    echo "📋 Sample API Calls:"
    echo "   curl http://localhost:8080/api/v1/crypto/version"
    echo "   curl http://localhost:8080/api/v1/crypto/market/BTC-USD"
    echo "   curl http://localhost:8080/api/v1/crypto/signals/BTC-USD"
    echo ""
    echo "📁 Log Files:"
    echo "   Application: application.log"
    echo "   Infrastructure: docker-compose logs"
    echo ""
    echo "🛑 To stop the platform:"
    echo "   ./stop-platform.sh"
    echo ""
}

# Create stop script
create_stop_script() {
    cat > stop-platform.sh << 'EOF'
#!/bin/bash

echo "🛑 Stopping Crypto Analytics Platform..."

# Stop application
if [ -f "app.pid" ]; then
    APP_PID=$(cat app.pid)
    if ps -p $APP_PID > /dev/null 2>&1; then
        echo "Stopping application (PID: $APP_PID)..."
        kill $APP_PID
        rm app.pid
        echo "✅ Application stopped"
    else
        echo "⚠️  Application process not found"
        rm app.pid
    fi
else
    echo "⚠️  Application PID file not found"
fi

# Stop infrastructure services
echo "Stopping infrastructure services..."
docker-compose down

echo "✅ Platform stopped successfully"
EOF

    chmod +x stop-platform.sh
}

# Handle script interruption
cleanup() {
    print_warning "Script interrupted. Cleaning up..."
    if [ ! -z "$APP_PID" ]; then
        kill $APP_PID 2>/dev/null || true
    fi
    exit 1
}

# Set trap for cleanup
trap cleanup INT TERM

# Main execution
main() {
    echo "Starting Crypto Analytics Platform setup..."
    echo "Current directory: $(pwd)"
    echo ""
    
    check_prerequisites
    echo ""
    
    start_infrastructure
    echo ""
    
    build_application
    echo ""
    
    start_application
    echo ""
    
    create_stop_script
    
    show_startup_info
}

# Run main function
main "$@"
