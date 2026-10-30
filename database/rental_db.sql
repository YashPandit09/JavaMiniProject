-- =====================================================================
--  Online Vehicle Rental Management System
--  Database : rental_db   (MySQL 8.0)
--
--  How to run : MySQL Workbench > File > Open SQL Script > this file
--               then click the lightning icon (Execute all).
--  WARNING    : running it again DROPS rental_db and resets all data.
--
--  Sample dates are relative to CURDATE(), so the demo data always
--  has past rentals, vehicles on rent today and upcoming bookings.
-- =====================================================================

DROP DATABASE IF EXISTS rental_db;
CREATE DATABASE rental_db;
USE rental_db;

-- ---------------------------------------------------------------------
-- users : staff logins only (customers do not log in)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(30) NOT NULL UNIQUE,
    password  VARCHAR(50) NOT NULL,              -- plain text for the demo (see Limitations)
    role      VARCHAR(10) NOT NULL,
    CHECK (role IN ('ADMIN', 'STAFF'))
);

-- ---------------------------------------------------------------------
-- customers
-- ---------------------------------------------------------------------
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    phone       CHAR(10)    NOT NULL,
    license_no  VARCHAR(20) UNIQUE               -- can be NULL; booking is refused without it
);

-- ---------------------------------------------------------------------
-- vehicles
--   status AVAILABLE   = can be booked
--   status MAINTENANCE = cannot be booked
--   ("on rent" is worked out from the bookings table, not stored here)
-- ---------------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id   INT AUTO_INCREMENT PRIMARY KEY,
    reg_no       VARCHAR(15)  NOT NULL UNIQUE,
    make_model   VARCHAR(50)  NOT NULL,
    type         VARCHAR(10)  NOT NULL,
    rate_per_day DECIMAL(8,2) NOT NULL,
    status       VARCHAR(12)  NOT NULL DEFAULT 'AVAILABLE',
    CHECK (type IN ('CAR', 'SUV', 'BIKE', 'VAN')),
    CHECK (rate_per_day > 0),
    CHECK (status IN ('AVAILABLE', 'MAINTENANCE'))
);

-- ---------------------------------------------------------------------
-- bookings
--   status BOOKED    = confirmed, vehicle not yet returned
--   status RETURNED  = closed, payment saved
--   status CANCELLED = cancelled before return
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    booking_id  INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT  NOT NULL,
    vehicle_id  INT  NOT NULL,
    from_date   DATE NOT NULL,
    to_date     DATE NOT NULL,
    total_amt   DECIMAL(10,2) NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'BOOKED',
    CHECK (to_date > from_date),
    CHECK (status IN ('BOOKED', 'RETURNED', 'CANCELLED')),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (vehicle_id)  REFERENCES vehicles(vehicle_id)
);

-- ---------------------------------------------------------------------
-- payments : one row is saved when a vehicle is returned
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount     DECIMAL(10,2) NOT NULL,
    paid_on    DATE NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);


-- =====================================================================
--  SAMPLE DATA
-- =====================================================================

INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'ADMIN'),
('staff', 'staff123', 'STAFF');

INSERT INTO customers (customer_id, name, phone, license_no) VALUES
(1, 'Rohan Mehta',     '9876543210', 'MH1220190012345'),
(2, 'Priya Sharma',    '9823456781', 'MH1420180067890'),
(3, 'Aditya Kulkarni', '9765432109', 'MH1220210034567'),
(4, 'Sneha Patil',     '9890123456', 'MH0420170098765'),
(5, 'Karan Joshi',     '9812345670', 'GJ0120200045678'),
(6, 'Neha Deshmukh',   '9900112233', NULL);          -- no licence yet: booking is refused

INSERT INTO vehicles (vehicle_id, reg_no, make_model, type, rate_per_day, status) VALUES
( 1, 'MH12AB1234', 'Maruti Suzuki Swift',       'CAR',  1500.00, 'AVAILABLE'),
( 2, 'MH12CD5678', 'Hyundai i20',               'CAR',  1800.00, 'AVAILABLE'),
( 3, 'MH14EF9012', 'Honda City',                'CAR',  2500.00, 'AVAILABLE'),
( 4, 'MH12GH3456', 'Mahindra XUV700',           'SUV',  4000.00, 'AVAILABLE'),
( 5, 'MH14JK7890', 'Toyota Innova Crysta',      'SUV',  4500.00, 'AVAILABLE'),
( 6, 'MH12LM2345', 'Tata Nexon EV',             'SUV',  3200.00, 'MAINTENANCE'),
( 7, 'MH12NP6789', 'Royal Enfield Classic 350', 'BIKE',  900.00, 'AVAILABLE'),
( 8, 'MH14QR1122', 'Honda Activa 6G',           'BIKE',  500.00, 'AVAILABLE'),
( 9, 'MH12ST3344', 'Bajaj Pulsar NS200',        'BIKE',  800.00, 'AVAILABLE'),
(10, 'MH14UV5566', 'Force Traveller 17-Seater', 'VAN',  5500.00, 'AVAILABLE');

-- total_amt = days x rate_per_day   (days = to_date - from_date)
INSERT INTO bookings (booking_id, customer_id, vehicle_id, from_date, to_date, total_amt, status) VALUES
-- past rentals, already returned and paid
( 1, 1,  1, CURDATE() - INTERVAL 95 DAY, CURDATE() - INTERVAL 92 DAY,  4500.00, 'RETURNED'),
( 2, 2,  4, CURDATE() - INTERVAL 80 DAY, CURDATE() - INTERVAL 76 DAY, 16000.00, 'RETURNED'),
( 3, 3,  7, CURDATE() - INTERVAL 62 DAY, CURDATE() - INTERVAL 60 DAY,  1800.00, 'RETURNED'),
( 4, 4, 10, CURDATE() - INTERVAL 50 DAY, CURDATE() - INTERVAL 47 DAY, 16500.00, 'RETURNED'),
( 5, 5,  3, CURDATE() - INTERVAL 35 DAY, CURDATE() - INTERVAL 31 DAY, 10000.00, 'RETURNED'),
( 6, 1,  5, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 17 DAY, 13500.00, 'RETURNED'),
( 7, 2,  8, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL  8 DAY,  1000.00, 'RETURNED'),
-- a cancelled booking (no payment)
( 8, 3,  2, CURDATE() - INTERVAL 12 DAY, CURDATE() - INTERVAL 10 DAY,  3600.00, 'CANCELLED'),
-- on rent today
( 9, 4,  2, CURDATE() - INTERVAL  2 DAY, CURDATE() + INTERVAL  2 DAY,  7200.00, 'BOOKED'),
(10, 5,  7, CURDATE() - INTERVAL  1 DAY, CURDATE() + INTERVAL  1 DAY,  1800.00, 'BOOKED'),
-- overdue: return date has passed but the vehicle is not back yet
(11, 2,  9, CURDATE() - INTERVAL  6 DAY, CURDATE() - INTERVAL  1 DAY,  4000.00, 'BOOKED'),
-- upcoming bookings
(12, 1,  3, CURDATE() + INTERVAL  3 DAY, CURDATE() + INTERVAL  6 DAY,  7500.00, 'BOOKED'),
(13, 2,  4, CURDATE() + INTERVAL  5 DAY, CURDATE() + INTERVAL  8 DAY, 12000.00, 'BOOKED');

INSERT INTO payments (booking_id, amount, paid_on) VALUES
(1,  4500.00, CURDATE() - INTERVAL 92 DAY),
(2, 16000.00, CURDATE() - INTERVAL 76 DAY),
(3,  1800.00, CURDATE() - INTERVAL 60 DAY),
(4, 16500.00, CURDATE() - INTERVAL 47 DAY),
(5, 10000.00, CURDATE() - INTERVAL 31 DAY),
(6, 13500.00, CURDATE() - INTERVAL 17 DAY),
(7,  1000.00, CURDATE() - INTERVAL  8 DAY);

-- quick check
SELECT 'users' AS table_name, COUNT(*) AS total_rows FROM users
UNION ALL SELECT 'customers', COUNT(*) FROM customers
UNION ALL SELECT 'vehicles',  COUNT(*) FROM vehicles
UNION ALL SELECT 'bookings',  COUNT(*) FROM bookings
UNION ALL SELECT 'payments',  COUNT(*) FROM payments;
