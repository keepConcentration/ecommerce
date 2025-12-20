-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS ecommerce_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON ecommerce.* TO 'sa'@'%';

FLUSH PRIVILEGES;
