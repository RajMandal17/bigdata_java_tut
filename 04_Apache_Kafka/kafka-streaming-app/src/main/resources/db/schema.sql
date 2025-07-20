-- PostgreSQL Database Schema for Kafka Streaming Application
-- This script creates all necessary tables for the application

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS retry_attempts CASCADE;
DROP TABLE IF EXISTS failed_messages CASCADE;
DROP TABLE IF EXISTS fraud_detections CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- Create customers table
CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    daily_limit DECIMAL(15,2) DEFAULT 5000.00,
    monthly_limit DECIMAL(15,2) DEFAULT 50000.00,
    total_spending DECIMAL(15,2) DEFAULT 0.00,
    last_transaction_time TIMESTAMP,
    block_reason VARCHAR(255),
    blocked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on customer status for faster queries
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_email ON customers(email);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    transaction_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_transactions_customer_id ON transactions(customer_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_transaction_time ON transactions(transaction_time);
CREATE INDEX idx_transactions_category ON transactions(category);
CREATE INDEX idx_transactions_amount ON transactions(amount);
CREATE UNIQUE INDEX idx_transactions_transaction_id ON transactions(transaction_id);

-- Create fraud_detections table
CREATE TABLE fraud_detections (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    fraud_type VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    amount DECIMAL(15,2),
    category VARCHAR(100),
    confidence_score DECIMAL(3,2) DEFAULT 0.95,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

-- Create indexes for fraud detection queries
CREATE INDEX idx_fraud_detections_customer_id ON fraud_detections(customer_id);
CREATE INDEX idx_fraud_detections_fraud_type ON fraud_detections(fraud_type);
CREATE INDEX idx_fraud_detections_detected_at ON fraud_detections(detected_at);

-- Create failed_messages table for dead letter queue
CREATE TABLE failed_messages (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    partition_id INTEGER,
    offset_value BIGINT,
    message_key TEXT,
    message_value TEXT,
    error_message TEXT,
    error_type VARCHAR(100),
    status VARCHAR(20) DEFAULT 'FAILED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Create indexes for failed messages
CREATE INDEX idx_failed_messages_topic ON failed_messages(topic);
CREATE INDEX idx_failed_messages_status ON failed_messages(status);
CREATE INDEX idx_failed_messages_created_at ON failed_messages(created_at);

-- Create retry_attempts table
CREATE TABLE retry_attempts (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    partition_id INTEGER,
    offset_value BIGINT,
    message_key TEXT,
    error_type VARCHAR(100),
    error_message TEXT,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for retry attempts
CREATE INDEX idx_retry_attempts_topic ON retry_attempts(topic);
CREATE INDEX idx_retry_attempts_attempt_time ON retry_attempts(attempt_time);

-- Insert sample customers
INSERT INTO customers (customer_id, first_name, last_name, email, phone, daily_limit, monthly_limit) VALUES
('CUST001', 'John', 'Doe', 'john.doe@email.com', '+1-555-0101', 3000.00, 30000.00),
('CUST002', 'Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', 5000.00, 50000.00),
('CUST003', 'Bob', 'Johnson', 'bob.johnson@email.com', '+1-555-0103', 2000.00, 20000.00),
('CUST004', 'Alice', 'Williams', 'alice.williams@email.com', '+1-555-0104', 10000.00, 100000.00),
('CUST005', 'Charlie', 'Brown', 'charlie.brown@email.com', '+1-555-0105', 1500.00, 15000.00),
('USER001', 'David', 'Davis', 'david.davis@email.com', '+1-555-0106', 4000.00, 40000.00),
('USER002', 'Eva', 'Miller', 'eva.miller@email.com', '+1-555-0107', 6000.00, 60000.00),
('USER003', 'Frank', 'Wilson', 'frank.wilson@email.com', '+1-555-0108', 3500.00, 35000.00),
('USER004', 'Grace', 'Moore', 'grace.moore@email.com', '+1-555-0109', 7000.00, 70000.00),
('USER005', 'Henry', 'Taylor', 'henry.taylor@email.com', '+1-555-0110', 2500.00, 25000.00);

-- Insert sample transactions
INSERT INTO transactions (transaction_id, customer_id, amount, category, transaction_time, status) VALUES
('TXN001', 'CUST001', 150.75, 'GROCERY', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'COMPLETED'),
('TXN002', 'CUST002', 89.50, 'RESTAURANT', CURRENT_TIMESTAMP - INTERVAL '1 hour', 'COMPLETED'),
('TXN003', 'CUST003', 45.25, 'GAS_STATION', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 'COMPLETED'),
('TXN004', 'CUST001', 1200.00, 'ONLINE_SHOPPING', CURRENT_TIMESTAMP - INTERVAL '15 minutes', 'COMPLETED'),
('TXN005', 'CUST004', 25000.00, 'REAL_ESTATE', CURRENT_TIMESTAMP - INTERVAL '5 minutes', 'PENDING'),
('TXN006', 'CUST002', 75.30, 'PHARMACY', CURRENT_TIMESTAMP - INTERVAL '3 minutes', 'COMPLETED'),
('TXN007', 'CUST005', 500.00, 'ATM', CURRENT_TIMESTAMP - INTERVAL '1 minute', 'COMPLETED');

-- Update customer spending totals
UPDATE customers SET 
    total_spending = (
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE transactions.customer_id = customers.customer_id 
        AND status = 'COMPLETED'
    ),
    last_transaction_time = (
        SELECT MAX(transaction_time) 
        FROM transactions 
        WHERE transactions.customer_id = customers.customer_id
    );

-- Create triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_customers_updated_at 
    BEFORE UPDATE ON customers 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at 
    BEFORE UPDATE ON transactions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create views for analytics
CREATE OR REPLACE VIEW daily_transaction_summary AS
SELECT 
    DATE(transaction_time) as transaction_date,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount
FROM transactions 
WHERE status = 'COMPLETED'
GROUP BY DATE(transaction_time)
ORDER BY transaction_date DESC;

CREATE OR REPLACE VIEW customer_spending_summary AS
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    c.total_spending,
    c.daily_limit,
    c.monthly_limit,
    COUNT(t.id) as transaction_count,
    MAX(t.transaction_time) as last_transaction_time,
    CASE 
        WHEN c.total_spending > c.monthly_limit * 0.9 THEN 'HIGH_SPENDER'
        WHEN c.total_spending > c.monthly_limit * 0.5 THEN 'MEDIUM_SPENDER'
        ELSE 'LOW_SPENDER'
    END as spending_category
FROM customers c
LEFT JOIN transactions t ON c.customer_id = t.customer_id AND t.status = 'COMPLETED'
GROUP BY c.customer_id, c.first_name, c.last_name, c.email, c.total_spending, c.daily_limit, c.monthly_limit;

CREATE OR REPLACE VIEW fraud_detection_summary AS
SELECT 
    fraud_type,
    COUNT(*) as detection_count,
    DATE(detected_at) as detection_date,
    AVG(confidence_score) as avg_confidence_score
FROM fraud_detections 
WHERE detected_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY fraud_type, DATE(detected_at)
ORDER BY detection_date DESC, detection_count DESC;

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO kafka_app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO kafka_app_user;

-- Display table information
SELECT 
    'Database schema created successfully!' as status,
    COUNT(*) as customer_count
FROM customers;

SELECT 
    'Sample data inserted successfully!' as status,
    COUNT(*) as transaction_count
FROM transactions;
