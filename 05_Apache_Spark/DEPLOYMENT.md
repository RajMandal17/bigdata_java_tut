# Apache Spark Big Data Platform - Deployment Guide

This guide provides comprehensive instructions for deploying the Apache Spark Big Data Platform in various environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Deployment](#docker-deployment)
4. [Production Deployment](#production-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Hardware Requirements

**Minimum:**
- CPU: 4 cores
- RAM: 8GB
- Storage: 50GB
- Network: 1Gbps

**Recommended:**
- CPU: 8+ cores
- RAM: 16GB+
- Storage: 100GB+ SSD
- Network: 10Gbps

### Software Requirements

- Java 17+
- Apache Spark 3.5+
- Docker 20.10+
- Docker Compose 2.0+
- Maven 3.8+
- Python 3.8+ (for PySpark notebooks)

## Local Development Setup

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd 05_Apache_Spark

# Build the application
mvn clean package -DskipTests

# Start the services
./start.sh
```

### 2. Verify Installation

```bash
# Test all APIs
./test-apis-enhanced.sh

# Check health
curl http://localhost:8080/actuator/health

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Docker Deployment

### 1. Environment Variables

Create `.env` file:

```bash
# Spark Configuration
SPARK_MASTER_HOST=spark-master
SPARK_MASTER_PORT=7077
SPARK_MASTER_WEBUI_PORT=8080
SPARK_WORKER_CORES=2
SPARK_WORKER_MEMORY=2g

# Application Configuration
APP_PORT=8080
APP_PROFILE=docker

# Database Configuration
POSTGRES_DB=spark_data
POSTGRES_USER=spark_user
POSTGRES_PASSWORD=spark_password

# Kafka Configuration
KAFKA_BROKER=kafka:9092
KAFKA_ZOOKEEPER=zookeeper:2181

# Monitoring
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_PASSWORD=admin123
```

### 2. Docker Compose Deployment

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f spark-app

# Scale workers
docker-compose up -d --scale spark-worker=3
```

### 3. Service URLs

- **Application**: http://localhost:8080
- **Spark Master UI**: http://localhost:4040
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Jupyter**: http://localhost:8888
- **Kafka UI**: http://localhost:8081

## Production Deployment

### 1. Infrastructure Setup

#### AWS EC2 Deployment

```bash
# Launch EC2 instances
aws ec2 run-instances \
    --image-id ami-0abcdef1234567890 \
    --instance-type m5.2xlarge \
    --key-name my-key-pair \
    --security-groups spark-cluster-sg \
    --count 3

# Configure security groups
aws ec2 authorize-security-group-ingress \
    --group-name spark-cluster-sg \
    --protocol tcp \
    --port 7077 \
    --cidr 0.0.0.0/0
```

#### On-Premises Cluster

```bash
# Install Docker on all nodes
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Setup Docker Swarm
docker swarm init --advertise-addr <MANAGER-IP>
docker swarm join --token <TOKEN> <MANAGER-IP>:2377
```

### 2. Production Configuration

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  spark-master:
    image: bitnami/spark:3.5
    environment:
      - SPARK_MODE=master
      - SPARK_MASTER_HOST=0.0.0.0
      - SPARK_MASTER_PORT=7077
    ports:
      - "7077:7077"
      - "8080:8080"
    volumes:
      - spark_data:/opt/bitnami/spark/data
    deploy:
      replicas: 1
      resources:
        limits:
          memory: 4G
          cpus: '2'

  spark-worker:
    image: bitnami/spark:3.5
    environment:
      - SPARK_MODE=worker
      - SPARK_MASTER_URL=spark://spark-master:7077
      - SPARK_WORKER_CORES=4
      - SPARK_WORKER_MEMORY=8g
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 10G
          cpus: '4'

  spark-app:
    image: spark-bigdata-app:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPARK_MASTER=spark://spark-master:7077
    ports:
      - "8080:8080"
    depends_on:
      - spark-master
      - postgres
      - kafka
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 4G
          cpus: '2'

volumes:
  spark_data:
  postgres_data:
  kafka_data:
```

### 3. SSL/TLS Configuration

```bash
# Generate certificates
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout spark.key -out spark.crt

# Configure HTTPS in application.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

## Kubernetes Deployment

### 1. Namespace and ConfigMap

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: spark-platform

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: spark-config
  namespace: spark-platform
data:
  spark-defaults.conf: |
    spark.master spark://spark-master:7077
    spark.sql.adaptive.enabled true
    spark.sql.adaptive.coalescePartitions.enabled true
    spark.serializer org.apache.spark.serializer.KryoSerializer
```

### 2. Spark Master Deployment

```yaml
# spark-master.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spark-master
  namespace: spark-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: spark-master
  template:
    metadata:
      labels:
        app: spark-master
    spec:
      containers:
      - name: spark-master
        image: bitnami/spark:3.5
        env:
        - name: SPARK_MODE
          value: "master"
        ports:
        - containerPort: 7077
        - containerPort: 8080
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"

---
apiVersion: v1
kind: Service
metadata:
  name: spark-master
  namespace: spark-platform
spec:
  selector:
    app: spark-master
  ports:
  - name: spark
    port: 7077
    targetPort: 7077
  - name: webui
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

### 3. Spark Worker Deployment

```yaml
# spark-worker.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spark-worker
  namespace: spark-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spark-worker
  template:
    metadata:
      labels:
        app: spark-worker
    spec:
      containers:
      - name: spark-worker
        image: bitnami/spark:3.5
        env:
        - name: SPARK_MODE
          value: "worker"
        - name: SPARK_MASTER_URL
          value: "spark://spark-master:7077"
        - name: SPARK_WORKER_CORES
          value: "4"
        - name: SPARK_WORKER_MEMORY
          value: "8g"
        resources:
          requests:
            memory: "6Gi"
            cpu: "3"
          limits:
            memory: "10Gi"
            cpu: "4"
```

### 4. Application Deployment

```yaml
# spark-app.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spark-app
  namespace: spark-platform
spec:
  replicas: 2
  selector:
    matchLabels:
      app: spark-app
  template:
    metadata:
      labels:
        app: spark-app
    spec:
      containers:
      - name: spark-app
        image: spark-bigdata-app:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: SPARK_MASTER
          value: "spark://spark-master:7077"
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"

---
apiVersion: v1
kind: Service
metadata:
  name: spark-app
  namespace: spark-platform
spec:
  selector:
    app: spark-app
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

### 5. Deploy to Kubernetes

```bash
# Apply all configurations
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f spark-master.yaml
kubectl apply -f spark-worker.yaml
kubectl apply -f spark-app.yaml

# Check deployment status
kubectl get pods -n spark-platform
kubectl get services -n spark-platform

# Scale workers
kubectl scale deployment spark-worker --replicas=5 -n spark-platform
```

## Monitoring and Maintenance

### 1. Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spark-app'
    static_configs:
      - targets: ['spark-app:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'spark-master'
    static_configs:
      - targets: ['spark-master:8080']

  - job_name: 'spark-workers'
    static_configs:
      - targets: ['spark-worker:8081']
```

### 2. Grafana Dashboards

Import dashboard JSON for:
- Spark Application Metrics
- JVM Metrics
- System Resource Usage
- Custom Business Metrics

### 3. Log Aggregation

```yaml
# filebeat.yml
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'
  processors:
  - add_docker_metadata:
      host: "unix:///var/run/docker.sock"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]

setup.kibana:
  host: "kibana:5601"
```

### 4. Backup Strategy

```bash
#!/bin/bash
# backup.sh

# Backup data volumes
docker run --rm -v spark_data:/data -v $(pwd):/backup \
    alpine tar czf /backup/spark_data_$(date +%Y%m%d).tar.gz /data

# Backup application configuration
kubectl get configmap -n spark-platform -o yaml > config_backup_$(date +%Y%m%d).yaml

# Backup database
docker exec postgres pg_dump -U spark_user spark_data > db_backup_$(date +%Y%m%d).sql
```

## Troubleshooting

### Common Issues

#### 1. Out of Memory Errors

```bash
# Increase executor memory
export SPARK_EXECUTOR_MEMORY=4g
export SPARK_DRIVER_MEMORY=2g

# Optimize garbage collection
export SPARK_EXECUTOR_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

#### 2. Connection Issues

```bash
# Check network connectivity
docker exec spark-worker ping spark-master

# Verify ports
netstat -tulpn | grep 7077

# Check firewall rules
sudo ufw status
```

#### 3. Performance Issues

```bash
# Enable adaptive query execution
spark.conf.set("spark.sql.adaptive.enabled", "true")

# Increase parallelism
spark.conf.set("spark.default.parallelism", "200")

# Optimize shuffle partitions
spark.conf.set("spark.sql.shuffle.partitions", "200")
```

### Diagnostic Commands

```bash
# Check Spark application status
curl http://localhost:8080/actuator/health

# View application logs
docker logs -f spark-app

# Monitor resource usage
docker stats

# Check Spark UI
open http://localhost:4040

# View cluster status
curl http://localhost:8080/api/spark/monitoring/metrics
```

### Log Analysis

```bash
# Search for errors in logs
grep -i error /var/log/spark/*.log

# Monitor application metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

## Security Considerations

### 1. Authentication

- Enable Spark authentication
- Configure database access controls
- Set up API authentication

### 2. Network Security

- Use firewalls and security groups
- Enable SSL/TLS for all communications
- Implement network segmentation

### 3. Data Security

- Encrypt data at rest and in transit
- Implement access controls
- Regular security audits

## Performance Tuning

### 1. Spark Configuration

```properties
# spark-defaults.conf
spark.executor.memory=8g
spark.executor.cores=4
spark.driver.memory=4g
spark.sql.adaptive.enabled=true
spark.sql.adaptive.coalescePartitions.enabled=true
spark.serializer=org.apache.spark.serializer.KryoSerializer
```

### 2. JVM Tuning

```bash
# JVM options for better performance
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseJVMCICompiler
```

This deployment guide provides comprehensive instructions for setting up the Apache Spark Big Data Platform in various environments, from local development to production Kubernetes clusters.
