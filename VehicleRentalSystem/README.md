# Online Vehicle Rental Management System

A web-based vehicle rental management application built using **Java Servlets**, **JSP**, **JDBC**, **MySQL 8.0**, and **Bootstrap 5**, deployed on **Apache Tomcat 10.1** and developed as an **Apache NetBeans / Maven WAR** project.

---

## 1. Project Overview

The **Online Vehicle Rental Management System** allows customers to:
- Register an account with secure password hashing (PBKDF2).
- Authenticate and manage active sessions with session fixation protection.
- Browse available vehicles (Cars, SUVs, Bikes) with filter options.
- Search vehicle availability dynamically by specifying pickup and return dates.
- Book a vehicle with atomic concurrency protection, server-side price calculation, and overlap collision checks.
- View personal booking history and cancel active bookings prior to the pickup date.

---

## 2. Technology Stack

| Component | Technology / Version |
| :--- | :--- |
| **Language** | Java 17 (LTS) |
| **Web Tier** | Jakarta Servlet 6.0 (`jakarta.servlet.*`), JSP 3.1, JSTL 3.0.1 |
| **Persistence** | JDBC via `com.mysql.cj.jdbc.Driver` |
| **Database** | MySQL 8.0+ (InnoDB Storage Engine) |
| **Servlet Container** | Apache Tomcat 10.1.x (Jakarta EE 10 compliant) |
| **Build & Dependency Management** | Apache Maven (WAR packaging) |
| **IDE** | Apache NetBeans 21+ / 25 |
| **Frontend** | HTML5, CSS3, Bootstrap 5.3.3, Bootstrap Icons 1.11.3, JavaScript |

> **Important:** This project strictly uses `jakarta.servlet.*` packages compatible with Tomcat 10.1+. No legacy `javax.servlet.*` packages are used.

---

## 3. Project Directory Structure

```text
VehicleRentalSystem/
├── pom.xml                               # Maven project configuration
├── README.md                             # Complete project documentation & guide
├── database/
│   └── schema.sql                        # Database initialization & sample inventory
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── vehiclerental/
        │           ├── DBConnection.java     # JDBC connection factory & config reader
        │           ├── PasswordUtil.java     # PBKDF2 password hashing & verification
        │           ├── LoginServlet.java     # Authentication, session lifecycle & logout
        │           ├── RegisterServlet.java  # User registration & input validation
        │           ├── VehicleServlet.java   # Inventory display & date overlap search
        │           └── BookingServlet.java   # Booking transactions, history & cancellation
        └── webapp/
            ├── index.jsp                     # Landing page with hero & quick search
            ├── login.jsp                     # User login interface
            ├── register.jsp                  # Account registration interface
            ├── vehicles.jsp                  # Fleet catalog & availability filter
            ├── booking.jsp                   # Booking form, confirmation & My Bookings
            ├── css/
            │   └── style.css                 # Custom design tokens & responsive CSS
            ├── images/
            │   └── README.txt                # Directory for local vehicle images
            └── WEB-INF/
                ├── web.xml                   # Deployment descriptor & error handlers
                └── error/
                    ├── 404.jsp               # Not Found error page
                    └── 500.jsp               # Internal Server Error page
```

---

## 4. Database Design & Schema

The database `vehicle_rental_db` is designed on MySQL InnoDB to support ACID transactions and referential integrity.

### Tables

1. **`users`**
   - `id`: `INT AUTO_INCREMENT PRIMARY KEY`
   - `full_name`: `VARCHAR(100) NOT NULL`
   - `email`: `VARCHAR(150) NOT NULL UNIQUE`
   - `password_hash`: `VARCHAR(256) NOT NULL` (PBKDF2 format: `iterations:salt:hash`)
   - `phone`: `VARCHAR(15) NOT NULL`
   - `role`: `ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER'`
   - `created_at`: `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

2. **`vehicles`**
   - `id`: `INT AUTO_INCREMENT PRIMARY KEY`
   - `name`: `VARCHAR(100) NOT NULL`
   - `brand`: `VARCHAR(60) NOT NULL`
   - `model`: `VARCHAR(60) NOT NULL`
   - `vehicle_type`: `ENUM('CAR', 'SUV', 'BIKE') NOT NULL`
   - `registration_number`: `VARCHAR(20) NOT NULL UNIQUE`
   - `seating_capacity`: `INT NOT NULL`
   - `price_per_day`: `DECIMAL(10,2) NOT NULL`
   - `image_url`: `VARCHAR(255) DEFAULT NULL`
   - `description`: `TEXT`
   - `status`: `ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE'`

3. **`bookings`**
   - `id`: `INT AUTO_INCREMENT PRIMARY KEY`
   - `user_id`: `INT NOT NULL` (Foreign Key -> `users.id`)
   - `vehicle_id`: `INT NOT NULL` (Foreign Key -> `vehicles.id`)
   - `pickup_date`: `DATE NOT NULL`
   - `return_date`: `DATE NOT NULL`
   - `total_days`: `INT NOT NULL`
   - `total_amount`: `DECIMAL(10,2) NOT NULL`
   - `booking_status`: `ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED'`
   - `created_at`: `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### Overlap Rule Formula

An existing active booking conflicts with a requested reservation if:
$$\text{existing.pickup\_date} < \text{requested.return\_date} \quad \text{AND} \quad \text{existing.return\_date} > \text{requested.pickup\_date}$$
*(Only non-CANCELLED bookings are evaluated).*

---

## 5. Database Configuration & Setup

### Step 1: Run the SQL Schema
1. Open MySQL Command Line or MySQL Workbench.
2. Execute the script located at `database/schema.sql`:
   ```sql
   SOURCE y:/UAIU/Notes/SEM 5/Advanced Java Programming/MiniProject/VehicleRentalSystem/database/schema.sql;
   ```
   Or open `database/schema.sql` in MySQL Workbench and execute the full script.

### Step 2: Configure Database Credentials
The application reads database configuration via `DBConnection.java`.

#### Option A: Set Environment Variables (Recommended)
On Windows:
```cmd
setx VEHICLE_RENTAL_DB_URL "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
setx VEHICLE_RENTAL_DB_USER "root"
setx VEHICLE_RENTAL_DB_PASS "your_mysql_password"
```

#### Option B: Configure in Tomcat Environment (`setenv.bat`)
Create or edit `<TOMCAT_HOME>\bin\setenv.bat`:
```bat
set "VEHICLE_RENTAL_DB_URL=jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
set "VEHICLE_RENTAL_DB_USER=root"
set "VEHICLE_RENTAL_DB_PASS=your_mysql_password"
```

#### Option C: Default Values in `DBConnection.java`
If environment variables are omitted, `DBConnection.java` falls back to:
- URL: `jdbc:mysql://localhost:3306/vehicle_rental_db`
- User: `root`
- Password: `""` (empty string)

---

## 6. How to Import and Run in Apache NetBeans

### Prerequisites
1. **JDK 17 or higher** installed.
2. **Apache NetBeans 21+ / 25**.
3. **Apache Tomcat 10.1.x** installed and registered in NetBeans:
   - In NetBeans, open **Tools > Servers**.
   - Click **Add Server...**, choose **Apache Tomcat or TomEE**.
   - Browse to your Tomcat 10.1 installation directory.
   - Enter your Tomcat manager username and password.

### Importing into NetBeans
1. Launch **Apache NetBeans**.
2. Click **File > Open Project...**
3. Navigate to:
   `y:\UAIU\Notes\SEM 5\Advanced Java Programming\MiniProject\VehicleRentalSystem`
4. NetBeans will detect the project by its `pom.xml` (Maven icon). Select it and click **Open Project**.
5. Wait for NetBeans to load dependencies.

### Deploying and Running on Tomcat 10.1
1. Right-click the **VehicleRentalSystem** project in the Projects panel.
2. Select **Properties**.
3. In **Run** category:
   - **Server**: Select **Apache Tomcat 10.1**.
   - **Context Path**: `/VehicleRentalSystem`
4. Click **OK**.
5. Right-click the project and select **Run** (or press `F6`).
6. NetBeans compiles the project using Maven, builds `VehicleRentalSystem.war`, deploys it to Tomcat 10.1, and opens your default browser at:
   ```text
   http://localhost:8080/VehicleRentalSystem/
   ```

### Command-Line Maven Build (Optional)
If building outside NetBeans:
```powershell
mvn clean package
```
The resulting WAR file is produced at `target/VehicleRentalSystem.war` and can be copied directly to `<TOMCAT_HOME>/webapps/`.

---

## 7. Architecture & Java Class Explanation

### 1. `DBConnection.java`
- Acts as the centralized connection factory using `DriverManager`.
- Loads `com.mysql.cj.jdbc.Driver` dynamically in a `static` initializer.
- Reads connection properties (`VEHICLE_RENTAL_DB_URL`, `VEHICLE_RENTAL_DB_USER`, `VEHICLE_RENTAL_DB_PASS`) safely from environment variables before falling back to defaults.
- All servlets consume connections through `try (Connection conn = DBConnection.getConnection())` ensuring resource closure.

### 2. `PasswordUtil.java`
- Implements secure PBKDF2 password hashing using standard JDK cryptography (`javax.crypto.SecretKeyFactory` with `PBKDF2WithHmacSHA256`).
- Uses 210,000 iterations and a 16-byte cryptographically secure random salt generated by `SecureRandom`.
- Encodes output as `iterations:base64(salt):base64(hash)`.
- Compares password hashes in constant time (`slowEquals`) to prevent timing side-channel attacks.
- Requires no external third-party security libraries.

### 3. `LoginServlet.java`
- Mapped to `/login` and `/logout`.
- `GET /login`: Forwards to `login.jsp`.
- `POST /login`: Validates inputs, fetches user by email using a `PreparedStatement`, verifies password using `PasswordUtil.verifyPassword()`.
- **Session Fixation Defense**: Calls `session.invalidate()` on existing sessions before instantiating a fresh session upon successful authentication.
- Binds `userId`, `userName`, `userEmail`, and `userRole` to the session.
- `GET /logout`: Invalidates the session and redirects to `/login?loggedout=true`.

### 4. `RegisterServlet.java`
- Mapped to `/register`.
- `GET /register`: Forwards to `register.jsp`.
- `POST /register`: Performs comprehensive server-side input validation (full name length, regex email format, 10–15 digit phone, minimum 6 characters password, password match).
- Checks for email uniqueness in MySQL before inserting.
- Hashes passwords using `PasswordUtil.hashPassword()`.
- Inserts new customer records with default role `CUSTOMER`.
- Redirects to `/login?registered=true` upon successful registration.

### 5. `VehicleServlet.java`
- Mapped to `/vehicles`.
- `GET /vehicles`: Queries available vehicles from MySQL.
- Supports type filtering (`CAR`, `SUV`, `BIKE`).
- Supports date availability filtering using an optimized `NOT EXISTS` SQL subquery applying the overlap formula.
- Automatically packs vehicles as list of maps and forwards to `vehicles.jsp`.

### 6. `BookingServlet.java`
- Mapped to `/booking`.
- **Security & Authorization**: Requires an active session; redirects unauthenticated visitors to `/login?redirected=true`.
- **Atomic Concurrency Protection**:
  1. Sets connection to `SERIALIZABLE` transaction isolation and `autoCommit = false`.
  2. Queries vehicle availability and locks conflicting booking records using `SELECT ... FOR UPDATE`.
  3. Validates date ordering (pickup must not be in past, return must be after pickup).
  4. Calculates days and total amount exclusively on the server (client price data is never trusted).
  5. Inserts booking record, commits transaction, or rolls back safely on any collision or SQL error.
- `GET /booking?action=mybookings`: Displays all bookings belonging strictly to the logged-in user.
- `POST /booking?action=cancel`: Validates that the booking exists, belongs to the logged-in user, is currently `CONFIRMED`, and the pickup date has not passed before transitioning status to `CANCELLED`.

---

## 8. Safe Administrator Setup Procedure

To comply with security requirements, no hardcoded admin account with default credentials exists in the code or SQL script.

To promote any registered user to an administrator:
1. Register a new user normally via the web interface (`/register`), for example `admin@vehiclerental.com`.
2. Connect to MySQL and update the user's role:
   ```sql
   USE vehicle_rental_db;
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@vehiclerental.com';
   ```
3. Log out and log back in. The session now carries `userRole = ADMIN`.

---

## 9. Manual Testing Checklist

| # | Test Scenario | Steps | Expected Result |
| :--- | :--- | :--- | :--- |
| **1** | **User Registration (Valid)** | Navigate to `/register`, enter valid name, email, 10-digit phone, and matching password (>= 6 chars). Click Register. | Redirects to `/login?registered=true` displaying a green success message. Password in DB is PBKDF2 hashed. |
| **2** | **Registration Validation (Invalid)** | Try submitting with mismatched passwords, invalid email, or short password. | Form reloads displaying clear error message; entered name/email/phone remain preserved. |
| **3** | **Duplicate Email Registration** | Register with an email that already exists. | Displays error: *"An account with this email already exists."* |
| **4** | **User Login (Valid)** | Enter registered email and password on `/login`. | Successful login; redirected to `/vehicles`; navbar shows user's name, "My Bookings", and "Logout". |
| **5** | **User Login (Invalid)** | Enter incorrect password or non-existent email. | Displays error: *"Invalid email or password."* No user credentials or database errors are exposed. |
| **6** | **Session Logout** | Click "Logout" in navbar. | Session invalidated; redirected to `/login?loggedout=true`. Accessing `/booking` afterwards requires re-login. |
| **7** | **Vehicle Catalog & Filtering** | Go to `/vehicles`. Filter by type (e.g., `SUV`). | Displays only vehicles matching the selected category with accurate prices and seat count. |
| **8** | **Date Availability Search** | Select pickup and return dates on `/vehicles` where a vehicle is already booked. | Vehicles with conflicting confirmed bookings are excluded from search results. |
| **9** | **Booking Creation** | Click "Book Now" on a vehicle, select pickup tomorrow and return 3 days later. Click Confirm. | Booking confirmation screen appears displaying Booking ID, vehicle name, exact dates, calculated days, and total price. |
| **10** | **Invalid Date Validation** | Attempt to book with return date earlier than pickup date or pickup date in the past. | Server rejects booking with error message: *"Return date must be after pickup date."* or *"Pickup date cannot be in the past."* |
| **11** | **Overlap Collision Prevention** | In another browser/session, attempt to book the same vehicle for the identical overlapping dates. | Booking fails with message: *"This vehicle is already booked for the selected dates."* Transaction rolls back cleanly. |
| **12** | **Booking Cancellation** | Go to "My Bookings". Click "Cancel" on an upcoming confirmed booking. | Booking status changes to `CANCELLED`; cancel button disappears; green confirmation alert appears. |
| **13** | **Unauthorized Cancellation** | Submit a cancellation request with another user's `bookingId`. | Server verifies ownership against session `userId` and rejects request with error message: *"You are not authorized to cancel this booking."* |
| **14** | **Unauthorized Booking Access** | Log out and attempt to navigate directly to `/booking?vehicleId=1`. | Redirected immediately to `/login?redirected=true` with notice *"Please log in to continue."* |
| **15** | **Custom Error Handling** | Visit non-existent URL `/VehicleRentalSystem/invalid-path`. | Custom `404.jsp` is displayed cleanly without exposing container stack traces or server version. |
