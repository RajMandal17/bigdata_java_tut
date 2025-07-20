#!/bin/bash

# Kafka Streaming Application Startup Script
# This script sets up and starts the entire Kafka streaming ecosystem

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to wait for service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=60
    local attempt=1

    print_status "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start within $(($max_attempts * 2)) seconds"
    return 1
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 17 or later."
        exit 1
    fi
    
    if ! command_exists mvn; then
        print_error "Maven is not installed. Please install Maven 3.8 or later."
        exit 1
    fi
    
    print_success "All prerequisites are met!"
}

# Function to start infrastructure
start_infrastructure() {
    print_status "Starting Kafka infrastructure..."
    
    # Start Docker services
    docker-compose up -d
    
    # Wait for services to be ready
    wait_for_service localhost 2181 "Zookeeper"
    wait_for_service localhost 9092 "Kafka"
    wait_for_service localhost 5432 "PostgreSQL"
    wait_for_service localhost 6379 "Redis"
    wait_for_service localhost 8081 "Schema Registry"
    wait_for_service localhost 8083 "Kafka Connect"
    wait_for_service localhost 8088 "KSQL Server"
    wait_for_service localhost 8080 "Kafka UI"
    wait_for_service localhost 9090 "Prometheus"
    wait_for_service localhost 3000 "Grafana"
    
    print_success "Infrastructure is running!"
}

# Function to setup database
setup_database() {
    print_status "Setting up database schema..."
    
    # Wait a bit more for PostgreSQL to be fully ready
    sleep 5
    
    # Run database schema
    if docker exec kafka-streaming-app-postgres-1 psql -U kafkauser -d kafkadb -f /docker-entrypoint-initdb.d/schema.sql > /dev/null 2>&1; then
        print_success "Database schema created successfully!"
    else
        print_warning "Database schema might already exist or there was an issue. Continuing..."
    fi
}

# Function to create Kafka topics
create_topics() {
    print_status "Creating Kafka topics..."
    
    topics=(
        "transactions-topic:3:1"
        "user-events-topic:3:1"
        "alerts-topic:3:1"
        "enriched-transactions-topic:3:1"
        "customer-analytics-topic:3:1"
        "category-stats-topic:3:1"
        "transactions-topic-dlt:3:1"
        "user-events-topic-dlt:3:1"
        "alerts-topic-dlt:3:1"
        "health-check-topic:1:1"
    )
    
    for topic_config in "${topics[@]}"; do
        IFS=':' read -r topic partitions replication <<< "$topic_config"
        
        docker exec kafka kafka-topics --create \
            --topic $topic \
            --partitions $partitions \
            --replication-factor $replication \
            --if-not-exists \
            --bootstrap-server localhost:9092 > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            print_success "Created topic: $topic"
        else
            print_warning "Topic $topic might already exist"
        fi
    done
}

# Function to build application
build_application() {
    print_status "Building Spring Boot application..."
    
    # Clean and build the application
    if mvn clean package -DskipTests; then
        print_success "Application built successfully!"
    else
        print_error "Failed to build application"
        exit 1
    fi
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    
    if mvn test; then
        print_success "All tests passed!"
    else
        print_warning "Some tests failed. Check the output above."
        read -p "Do you want to continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Stopping due to test failures"
            exit 1
        fi
    fi
}

# Function to start application
start_application() {
    print_status "Starting Spring Boot application..."
    
    # Start the application in background
    nohup java -jar target/kafka-streaming-app-*.jar > application.log 2>&1 &
    APP_PID=$!
    echo $APP_PID > app.pid
    
    # Wait for application to start
    wait_for_service localhost 8080 "Spring Boot Application"
    
    print_success "Application started with PID: $APP_PID"
}

# Function to setup monitoring
setup_monitoring() {
    print_status "Setting up monitoring dashboards..."
    
    # Import Grafana dashboard (if Grafana API is available)
    sleep 5
    
    # Try to import dashboard
    if curl -s -X POST \
        -H "Content-Type: application/json" \
        -d @monitoring/grafana-dashboard.json \
        http://admin:admin@localhost:3000/api/dashboards/db > /dev/null 2>&1; then
        print_success "Grafana dashboard imported!"
    else
        print_warning "Could not import Grafana dashboard automatically. Import manually from monitoring/grafana-dashboard.json"
    fi
}

# Function to show status
show_status() {
    print_status "System Status:"
    echo ""
    
    services=(
        "Zookeeper:localhost:2181"
        "Kafka:localhost:9092"
        "PostgreSQL:localhost:5432"
        "Redis:localhost:6379"
        "Schema Registry:localhost:8081"
        "Kafka Connect:localhost:8083"
        "KSQL Server:localhost:8088"
        "Kafka UI:localhost:8080"
        "Spring Boot App:localhost:8080"
        "Prometheus:localhost:9090"
        "Grafana:localhost:3000"
        "Elasticsearch:localhost:9200"
        "Kibana:localhost:5601"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -r name host port <<< "$service"
        if nc -z $host $port 2>/dev/null; then
            echo -e "  ${GREEN}✓${NC} $name - Running"
        else
            echo -e "  ${RED}✗${NC} $name - Not running"
        fi
    done
    
    echo ""
    print_status "Application URLs:"
    echo "  • Kafka UI: http://localhost:8080"
    echo "  • Application API: http://localhost:8080/api"
    echo "  • Prometheus: http://localhost:9090"
    echo "  • Grafana: http://localhost:3000 (admin/admin)"
    echo "  • Kibana: http://localhost:5601"
    echo ""
}

# Function to generate sample data
generate_sample_data() {
    print_status "Generating sample data..."
    
    if curl -s -X POST "http://localhost:8080/api/events/generate/sample?transactionCount=50&userEventCount=25" > /dev/null; then
        print_success "Sample data generated!"
    else
        print_warning "Could not generate sample data. Make sure the application is running."
    fi
}

# Function to stop services
stop_services() {
    print_status "Stopping services..."
    
    # Stop Spring Boot application
    if [ -f app.pid ]; then
        APP_PID=$(cat app.pid)
        if kill $APP_PID 2>/dev/null; then
            print_success "Spring Boot application stopped"
        fi
        rm -f app.pid
    fi
    
    # Stop Docker services
    docker-compose down
    print_success "Infrastructure stopped"
}

# Function to clean up
cleanup() {
    print_status "Cleaning up..."
    
    stop_services
    
    # Remove application log
    rm -f application.log
    
    # Clean Docker volumes (optional)
    read -p "Do you want to remove Docker volumes (this will delete all data)? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        print_success "Docker volumes removed"
    fi
}

# Function to show help
show_help() {
    echo "Kafka Streaming Application Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     - Start the complete system (default)"
    echo "  stop      - Stop all services"
    echo "  restart   - Restart all services"
    echo "  status    - Show system status"
    echo "  test      - Run tests only"
    echo "  build     - Build application only"
    echo "  logs      - Show application logs"
    echo "  sample    - Generate sample data"
    echo "  clean     - Clean up and remove all data"
    echo "  help      - Show this help message"
    echo ""
}

# Function to show logs
show_logs() {
    if [ -f application.log ]; then
        tail -f application.log
    else
        print_error "Application log not found. Is the application running?"
        exit 1
    fi
}

# Main script logic
main() {
    local command=${1:-start}
    
    case $command in
        start)
            check_prerequisites
            start_infrastructure
            setup_database
            create_topics
            build_application
            run_tests
            start_application
            setup_monitoring
            generate_sample_data
            show_status
            print_success "Kafka Streaming Application is fully operational!"
            ;;
        stop)
            stop_services
            ;;
        restart)
            stop_services
            sleep 5
            main start
            ;;
        status)
            show_status
            ;;
        test)
            run_tests
            ;;
        build)
            build_application
            ;;
        logs)
            show_logs
            ;;
        sample)
            generate_sample_data
            ;;
        clean)
            cleanup
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Handle script interruption
trap 'print_warning "Script interrupted. Run \"$0 stop\" to clean up."; exit 1' INT TERM

# Run main function
main "$@"
