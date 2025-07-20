-- Initialize PostgreSQL database for Spark application
CREATE SCHEMA IF NOT EXISTS spark_analytics;

-- Create tables for different data domains
CREATE TABLE IF NOT EXISTS spark_analytics.sales_data (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    customer_age INTEGER,
    customer_gender VARCHAR(10),
    customer_location VARCHAR(100),
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spark_analytics.streaming_events (
    id SERIAL PRIMARY KEY,
    event_id VARCHAR(50) UNIQUE NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    session_id VARCHAR(50),
    event_data JSONB,
    event_timestamp TIMESTAMP NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spark_analytics.ml_model_results (
    id SERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(20) NOT NULL,
    input_data JSONB,
    prediction_result JSONB,
    confidence_score DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spark_analytics.batch_job_logs (
    id SERIAL PRIMARY KEY,
    job_id VARCHAR(100) NOT NULL,
    job_name VARCHAR(200) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    records_processed BIGINT,
    error_message TEXT,
    job_config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_sales_data_transaction_date ON spark_analytics.sales_data(transaction_date);
CREATE INDEX idx_sales_data_customer_id ON spark_analytics.sales_data(customer_id);
CREATE INDEX idx_sales_data_category ON spark_analytics.sales_data(category);

CREATE INDEX idx_streaming_events_timestamp ON spark_analytics.streaming_events(event_timestamp);
CREATE INDEX idx_streaming_events_type ON spark_analytics.streaming_events(event_type);
CREATE INDEX idx_streaming_events_user_id ON spark_analytics.streaming_events(user_id);

CREATE INDEX idx_ml_results_model ON spark_analytics.ml_model_results(model_name, model_version);
CREATE INDEX idx_ml_results_created_at ON spark_analytics.ml_model_results(created_at);

CREATE INDEX idx_batch_logs_job_name ON spark_analytics.batch_job_logs(job_name);
CREATE INDEX idx_batch_logs_status ON spark_analytics.batch_job_logs(status);

-- Insert sample data
INSERT INTO spark_analytics.sales_data 
(transaction_id, customer_id, product_id, product_name, category, quantity, unit_price, total_amount, 
 transaction_date, customer_age, customer_gender, customer_location, payment_method)
VALUES 
('TXN001', 'CUST001', 'PROD001', 'Laptop', 'Electronics', 1, 1200.00, 1200.00, 
 '2024-01-15 10:30:00', 35, 'M', 'New York', 'Credit Card'),
('TXN002', 'CUST002', 'PROD002', 'Coffee Maker', 'Appliances', 2, 89.99, 179.98, 
 '2024-01-15 14:15:00', 28, 'F', 'California', 'Debit Card'),
('TXN003', 'CUST003', 'PROD003', 'Book - Data Science', 'Books', 3, 45.00, 135.00, 
 '2024-01-16 09:20:00', 42, 'M', 'Texas', 'PayPal'),
('TXN004', 'CUST001', 'PROD004', 'Wireless Mouse', 'Electronics', 1, 25.99, 25.99, 
 '2024-01-16 16:45:00', 35, 'M', 'New York', 'Credit Card'),
('TXN005', 'CUST004', 'PROD005', 'Running Shoes', 'Sports', 1, 120.00, 120.00, 
 '2024-01-17 11:00:00', 25, 'F', 'Florida', 'Credit Card');
