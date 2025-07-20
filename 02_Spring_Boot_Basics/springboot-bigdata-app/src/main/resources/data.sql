-- Sample data initialization script
INSERT INTO transactions (customer_id, amount, category, currency, merchant_id, description, status, timestamp) VALUES
('CUST_1001', 125.50, 'FOOD', 'USD', 'MERCHANT_001', 'Restaurant payment', 'APPROVED', '2024-01-15 12:30:00'),
('CUST_1002', 89.99, 'SHOPPING', 'USD', 'MERCHANT_002', 'Online purchase', 'APPROVED', '2024-01-15 14:15:00'),
('CUST_1003', 15.75, 'TRANSPORT', 'USD', 'MERCHANT_003', 'Taxi ride', 'APPROVED', '2024-01-15 16:45:00'),
('CUST_1001', 45.00, 'ENTERTAINMENT', 'USD', 'MERCHANT_004', 'Movie tickets', 'APPROVED', '2024-01-15 19:30:00'),
('CUST_1004', 250.00, 'UTILITIES', 'USD', 'MERCHANT_005', 'Electric bill', 'PENDING', '2024-01-15 10:00:00'),
('CUST_1002', 75.25, 'FOOD', 'USD', 'MERCHANT_001', 'Grocery store', 'APPROVED', '2024-01-16 11:20:00'),
('CUST_1005', 999.99, 'SHOPPING', 'USD', 'MERCHANT_006', 'Electronics purchase', 'PENDING', '2024-01-16 13:45:00'),
('CUST_1003', 25.50, 'TRANSPORT', 'USD', 'MERCHANT_007', 'Bus ticket', 'APPROVED', '2024-01-16 08:15:00'),
('CUST_1006', 150.00, 'ENTERTAINMENT', 'EUR', 'MERCHANT_008', 'Concert ticket', 'APPROVED', '2024-01-16 20:00:00'),
('CUST_1001', 35.75, 'FOOD', 'USD', 'MERCHANT_009', 'Fast food', 'APPROVED', '2024-01-17 12:00:00');
