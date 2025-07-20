#!/bin/bash

# Database Foundations Project Management Script
# Usage: ./manage.sh [start|stop|restart|status|logs|clean]

PROJECT_NAME="database-foundations-app"
COMPOSE_FILE="docker-compose.yml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}"
    echo "================================================="
    echo "    Database Foundations for Big Data"
    echo "================================================="
    echo -e "${NC}"
}

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed!"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed!"
        exit 1
    fi
}

start_services() {
    print_header
    print_status "Starting all services..."
    
    # Create external network if it doesn't exist
    if ! docker network ls | grep -q "bigdata-network"; then
        print_status "Creating external network..."
        docker network create bigdata-network
    fi
    
    # Start services
    docker-compose -f $COMPOSE_FILE up -d
    
    print_status "Waiting for services to start..."
    sleep 10
    
    # Check service status
    check_services
    
    print_status "Services started successfully!"
    print_service_urls
}

stop_services() {
    print_header
    print_status "Stopping all services..."
    docker-compose -f $COMPOSE_FILE down
    print_status "Services stopped successfully!"
}

restart_services() {
    print_header
    stop_services
    sleep 5
    start_services
}

check_services() {
    print_status "Checking service health..."
    
    services=("mysql:3306" "mongodb:27017" "redis:6379" "kafka:9092" "elasticsearch:9200")
    
    for service in "${services[@]}"; do
        name=${service%:*}
        port=${service#*:}
        
        if docker-compose ps | grep -q "$name.*Up"; then
            echo -e "  ✅ $name is running"
        else
            echo -e "  ❌ $name is not running"
        fi
    done
}

show_logs() {
    if [ -z "$2" ]; then
        print_status "Showing logs for all services..."
        docker-compose -f $COMPOSE_FILE logs -f
    else
        print_status "Showing logs for $2..."
        docker-compose -f $COMPOSE_FILE logs -f "$2"
    fi
}

show_status() {
    print_header
    print_status "Service Status:"
    docker-compose -f $COMPOSE_FILE ps
    
    echo ""
    print_status "Resource Usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
}

clean_all() {
    print_header
    print_warning "This will remove all containers, volumes, and networks!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up..."
        docker-compose -f $COMPOSE_FILE down -v --remove-orphans
        docker system prune -f
        print_status "Cleanup completed!"
    else
        print_status "Cleanup cancelled."
    fi
}

print_service_urls() {
    echo ""
    print_status "Service URLs:"
    echo "  📊 Kafka UI:        http://localhost:8080"
    echo "  🔍 Kibana:          http://localhost:5601"
    echo "  🔗 Elasticsearch:   http://localhost:9200"
    echo "  ⚡ Spark UI:        http://localhost:8081"
    echo "  🗄️  MySQL:           localhost:3306"
    echo "  📄 MongoDB:         localhost:27017"
    echo "  🚀 Redis:           localhost:6379"
    echo ""
    print_status "Application:"
    echo "  🌐 Spring Boot:     http://localhost:8090"
    echo "  📈 Health Check:    http://localhost:8090/actuator/health"
    echo "  📊 Metrics:         http://localhost:8090/actuator/metrics"
}

build_application() {
    print_header
    print_status "Building Spring Boot application..."
    
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
        print_status "Application built successfully!"
    else
        print_error "Maven is not installed!"
        exit 1
    fi
}

run_application() {
    print_header
    print_status "Running Spring Boot application..."
    
    if [ ! -f "target/${PROJECT_NAME}-1.0.0.jar" ]; then
        print_warning "Application not built. Building first..."
        build_application
    fi
    
    mvn spring-boot:run
}

run_tests() {
    print_header
    print_status "Running tests..."
    mvn test
}

show_help() {
    print_header
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Start all services"
    echo "  stop      Stop all services" 
    echo "  restart   Restart all services"
    echo "  status    Show service status"
    echo "  logs      Show logs for all services"
    echo "  logs <service>  Show logs for specific service"
    echo "  clean     Clean up all containers and volumes"
    echo "  build     Build Spring Boot application"
    echo "  run       Run Spring Boot application"
    echo "  test      Run tests"
    echo "  urls      Show service URLs"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 logs mysql"
    echo "  $0 status"
}

# Main script logic
check_docker

case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "$@"
        ;;
    clean)
        clean_all
        ;;
    build)
        build_application
        ;;
    run)
        run_application
        ;;
    test)
        run_tests
        ;;
    urls)
        print_service_urls
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
