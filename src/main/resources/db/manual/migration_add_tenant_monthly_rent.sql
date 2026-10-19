-- Run this ONCE against an existing BoardNest database that was created before monthly rent was added.
USE boarding_house_db;
ALTER TABLE tenants ADD COLUMN monthly_rent DECIMAL(10,2) NOT NULL DEFAULT 0.00;
