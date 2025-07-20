-- Database initialization script for Big Data Foundations
-- This script creates the necessary tables and indexes

USE bigdata_db;

-- Create partitioned transactions table
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    category VARCHAR(50),
    merchant_id VARCHAR(100),
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    currency VARCHAR(3) DEFAULT 'USD',
    description TEXT,
    PRIMARY KEY (id, transaction_date),
    INDEX idx_customer_date (customer_id, transaction_date),
    INDEX idx_amount (amount),
    INDEX idx_status_date (status, transaction_date),
    INDEX idx_category (category),
    INDEX idx_merchant (merchant_id)
) PARTITION BY RANGE (YEAR(transaction_date)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- Create daily transaction summary table
CREATE TABLE daily_transaction_summary (
    summary_date DATE PRIMARY KEY,
    total_transactions INT DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0.00,
    avg_amount DECIMAL(15,2) DEFAULT 0.00,
    max_amount DECIMAL(15,2) DEFAULT 0.00,
    min_amount DECIMAL(15,2) DEFAULT 0.00,
    categories JSON,
    status_breakdown JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_summary_date (summary_date),
    INDEX idx_total_amount (total_amount)
);

-- Create customer profiles table
CREATE TABLE customer_profiles (
    customer_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    registration_date DATE,
    customer_tier ENUM('bronze', 'silver', 'gold', 'platinum') DEFAULT 'bronze',
    total_transactions INT DEFAULT 0,
    total_spent DECIMAL(15,2) DEFAULT 0.00,
    avg_transaction_amount DECIMAL(15,2) DEFAULT 0.00,
    last_transaction_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_tier (customer_tier),
    INDEX idx_registration (registration_date)
);

-- Create indexes for analytics
CREATE INDEX idx_customer_category_date 
ON transactions (customer_id, category, transaction_date);

CREATE INDEX idx_analytics_covering 
ON transactions (transaction_date, category, amount, status);

-- Functional index for month/year analysis
CREATE INDEX idx_month_year 
ON transactions ((YEAR(transaction_date)), (MONTH(transaction_date)));

-- Insert sample data
INSERT INTO customer_profiles (customer_id, first_name, last_name, email, phone, registration_date, customer_tier) VALUES
('CUST001', 'John', 'Doe', 'john.doe@email.com', '+1234567890', '2024-01-15', 'gold'),
('CUST002', 'Jane', 'Smith', 'jane.smith@email.com', '+1234567891', '2024-02-20', 'silver'),
('CUST003', 'Bob', 'Johnson', 'bob.johnson@email.com', '+1234567892', '2024-03-10', 'bronze'),
('CUST004', 'Alice', 'Williams', 'alice.williams@email.com', '+1234567893', '2024-01-25', 'platinum'),
('CUST005', 'Charlie', 'Brown', 'charlie.brown@email.com', '+1234567894', '2024-04-05', 'silver');

-- Insert sample transactions
INSERT INTO transactions (customer_id, amount, transaction_date, transaction_time, category, merchant_id, status, currency, description) VALUES
('CUST001', 299.99, '2024-01-20', '2024-01-20 10:30:00', 'electronics', 'MERCH001', 'completed', 'USD', 'Smartphone purchase'),
('CUST001', 45.50, '2024-01-21', '2024-01-21 14:15:00', 'food', 'MERCH002', 'completed', 'USD', 'Restaurant meal'),
('CUST002', 89.99, '2024-02-25', '2024-02-25 09:20:00', 'clothing', 'MERCH003', 'completed', 'USD', 'Winter jacket'),
('CUST002', 12.99, '2024-02-26', '2024-02-26 16:45:00', 'entertainment', 'MERCH004', 'completed', 'USD', 'Movie tickets'),
('CUST003', 1299.99, '2024-03-15', '2024-03-15 11:00:00', 'electronics', 'MERCH001', 'completed', 'USD', 'Laptop computer'),
('CUST003', 25.75, '2024-03-16', '2024-03-16 13:30:00', 'food', 'MERCH005', 'completed', 'USD', 'Grocery shopping'),
('CUST004', 199.99, '2024-01-30', '2024-01-30 15:20:00', 'health', 'MERCH006', 'completed', 'USD', 'Fitness equipment'),
('CUST004', 75.00, '2024-02-01', '2024-02-01 10:10:00', 'beauty', 'MERCH007', 'completed', 'USD', 'Skincare products'),
('CUST005', 450.00, '2024-04-10', '2024-04-10 12:45:00', 'travel', 'MERCH008', 'pending', 'USD', 'Flight booking'),
('CUST005', 89.50, '2024-04-11', '2024-04-11 19:30:00', 'food', 'MERCH002', 'completed', 'USD', 'Fine dining'),
('CUST001', 599.99, '2024-07-15', '2024-07-15 14:20:00', 'electronics', 'MERCH001', 'completed', 'USD', 'Gaming console'),
('CUST002', 155.75, '2024-07-16', '2024-07-16 11:30:00', 'home', 'MERCH009', 'completed', 'USD', 'Home decor'),
('CUST003', 99.99, '2024-07-17', '2024-07-17 16:15:00', 'books', 'MERCH010', 'completed', 'USD', 'Technical books'),
('CUST004', 275.50, '2024-07-18', '2024-07-18 09:45:00', 'clothing', 'MERCH003', 'completed', 'USD', 'Summer wardrobe'),
('CUST005', 45.99, '2024-07-19', '2024-07-19 20:00:00', 'entertainment', 'MERCH004', 'completed', 'USD', 'Concert tickets');

-- Update customer profiles with transaction data
UPDATE customer_profiles cp
SET 
    total_transactions = (SELECT COUNT(*) FROM transactions t WHERE t.customer_id = cp.customer_id),
    total_spent = (SELECT COALESCE(SUM(amount), 0) FROM transactions t WHERE t.customer_id = cp.customer_id AND status = 'completed'),
    avg_transaction_amount = (SELECT COALESCE(AVG(amount), 0) FROM transactions t WHERE t.customer_id = cp.customer_id AND status = 'completed'),
    last_transaction_date = (SELECT MAX(transaction_date) FROM transactions t WHERE t.customer_id = cp.customer_id);

-- Create a view for transaction analytics
CREATE VIEW transaction_analytics AS
SELECT 
    DATE(transaction_date) as date,
    category,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount,
    MAX(amount) as max_amount,
    MIN(amount) as min_amount,
    COUNT(DISTINCT customer_id) as unique_customers
FROM transactions 
WHERE status = 'completed'
GROUP BY DATE(transaction_date), category
ORDER BY date DESC, total_amount DESC;

-- Create stored procedure for daily summary calculation
DELIMITER //
CREATE PROCEDURE CalculateDailySummary(IN target_date DATE)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    INSERT INTO daily_transaction_summary (
        summary_date,
        total_transactions,
        total_amount,
        avg_amount,
        max_amount,
        min_amount,
        categories,
        status_breakdown
    )
    SELECT 
        target_date,
        COUNT(*),
        COALESCE(SUM(amount), 0),
        COALESCE(AVG(amount), 0),
        COALESCE(MAX(amount), 0),
        COALESCE(MIN(amount), 0),
        JSON_OBJECT(
            'electronics', COALESCE(SUM(CASE WHEN category = 'electronics' THEN amount END), 0),
            'food', COALESCE(SUM(CASE WHEN category = 'food' THEN amount END), 0),
            'clothing', COALESCE(SUM(CASE WHEN category = 'clothing' THEN amount END), 0),
            'entertainment', COALESCE(SUM(CASE WHEN category = 'entertainment' THEN amount END), 0),
            'health', COALESCE(SUM(CASE WHEN category = 'health' THEN amount END), 0),
            'travel', COALESCE(SUM(CASE WHEN category = 'travel' THEN amount END), 0),
            'other', COALESCE(SUM(CASE WHEN category NOT IN ('electronics', 'food', 'clothing', 'entertainment', 'health', 'travel') THEN amount END), 0)
        ),
        JSON_OBJECT(
            'completed', COUNT(CASE WHEN status = 'completed' THEN 1 END),
            'pending', COUNT(CASE WHEN status = 'pending' THEN 1 END),
            'failed', COUNT(CASE WHEN status = 'failed' THEN 1 END),
            'refunded', COUNT(CASE WHEN status = 'refunded' THEN 1 END)
        )
    FROM transactions
    WHERE DATE(transaction_date) = target_date
    ON DUPLICATE KEY UPDATE
        total_transactions = VALUES(total_transactions),
        total_amount = VALUES(total_amount),
        avg_amount = VALUES(avg_amount),
        max_amount = VALUES(max_amount),
        min_amount = VALUES(min_amount),
        categories = VALUES(categories),
        status_breakdown = VALUES(status_breakdown),
        updated_at = CURRENT_TIMESTAMP;
    
    COMMIT;
END //
DELIMITER ;

-- Generate daily summaries for existing data
CALL CalculateDailySummary('2024-01-20');
CALL CalculateDailySummary('2024-01-21');
CALL CalculateDailySummary('2024-02-25');
CALL CalculateDailySummary('2024-02-26');
CALL CalculateDailySummary('2024-03-15');
CALL CalculateDailySummary('2024-03-16');
CALL CalculateDailySummary('2024-01-30');
CALL CalculateDailySummary('2024-02-01');
CALL CalculateDailySummary('2024-04-10');
CALL CalculateDailySummary('2024-04-11');
CALL CalculateDailySummary('2024-07-15');
CALL CalculateDailySummary('2024-07-16');
CALL CalculateDailySummary('2024-07-17');
CALL CalculateDailySummary('2024-07-18');
CALL CalculateDailySummary('2024-07-19');

SHOW TABLES;
SELECT COUNT(*) as total_transactions FROM transactions;
SELECT COUNT(*) as total_customers FROM customer_profiles;
SELECT COUNT(*) as daily_summaries FROM daily_transaction_summary;
