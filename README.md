# Online Vehicle Rental Management System

Advanced Java Programming (CSG5.52003) mini project.
Two applications share one MySQL database (`rental_db`): a **web app**
(Jakarta Servlets + JDBC, deployed on Tomcat) and a **desktop app** (Java
Swing + JDBC). A booking made in one is visible immediately in the other.

```
MiniProject/
├── database/
│   └── rental_db.sql         <- run this first (creates DB + sample data)
├── VehicleRentalWeb/         <- web app (Maven, packages to .war)
└── VehicleRentalDesktop/     <- desktop app (Maven, packages to runnable .jar)
```

Both apps share the same model / DAO / exception / `DBConnect` classes
(`com.rental.model`, `com.rental.dao`, `com.rental.exception`,
`com.rental.util.DBConnect`) - they are copied into each project rather than
published as a shared library, to keep each project simple enough to open
and explain on its own in NetBeans.

---

## 1. Prerequisites

| Tool | Version used | Notes |
|---|---|---|
| JDK | 17+ | `java -version` |
| MySQL | 8.0 | with MySQL Workbench, or any MySQL client |
| Apache Tomcat | 10.1.x | **must be 10.x** (Jakarta `jakarta.servlet.*`, not `javax.servlet.*`) |
| Maven | 3.9+ | NetBeans 12+ / 25 ships one at `<NetBeans>\netbeans\java\maven\bin\mvn.cmd` - no separate install needed |
| IDE | NetBeans 25 (or Eclipse / IntelliJ) | optional - both apps also build from the command line |

---

## 2. Database setup

1. Open **MySQL Workbench**, connect as `root`.
2. File → Open SQL Script → select `database/rental_db.sql`.
3. Click the lightning-bolt icon (Execute) to run the whole script.

This **drops and recreates** `rental_db` with:
- 2 staff logins (`admin` / `admin123` role **ADMIN**, `staff` / `staff123` role **STAFF**)
- 6 customers, 10 vehicles, ~13 bookings and 7 payments as sample data, with
  dates relative to today so the dashboard always has something to show
  (a vehicle on rent now, an upcoming booking, a cancelled booking, etc.)

### Point the apps at your database

Both projects read the connection details from one file each:

```
VehicleRentalWeb/src/main/java/com/rental/util/DBConnect.java
VehicleRentalDesktop/src/main/java/com/rental/util/DBConnect.java
```

```java
private static final String URL = "jdbc:mysql://localhost:3306/rental_db";
private static final String USER = "root";
private static final String PASSWORD = "yashloq";   // <-- change this
```

Edit `USER` / `PASSWORD` in **both files** if your MySQL login differs, then
rebuild (see below).

---

## 3. Web app (VehicleRentalWeb)

### Option A - NetBeans (recommended for the viva)

1. File → Open Project → select `VehicleRentalWeb`.
2. Tools → Servers → Add Server → **Apache Tomcat** → point it at your
   Tomcat 10.1 install folder.
3. Right-click the project → Properties → Run → Server → choose that Tomcat.
4. Right-click the project → **Run**. NetBeans builds the WAR, deploys it,
   and opens `http://localhost:8080/VehicleRentalWeb/` in your browser.

### Option B - command line

```powershell
cd VehicleRentalWeb
mvn package
copy target\VehicleRentalWeb.war <TOMCAT_HOME>\webapps\
<TOMCAT_HOME>\bin\startup.bat
```

Then open `http://localhost:8080/VehicleRentalWeb/`.

---

## 4. Desktop app (VehicleRentalDesktop)

### Option A - NetBeans

1. File → Open Project → select `VehicleRentalDesktop`.
2. Right-click the project → **Run** (runs `com.rental.Main`).

### Option B - command line

```powershell
cd VehicleRentalDesktop
mvn package
java -jar target\VehicleRentalDesktop.jar
```

`mvn package` produces a **shaded jar** - the MySQL driver and the FlatLaf
look-and-feel library are bundled inside, so the single jar file is all you
need to copy/run anywhere a JRE 17 is installed.

---

## 5. Demo logins

| Username | Password | Role | Can add/edit vehicles? |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Yes |
| `staff` | `staff123` | STAFF | No (read-only on Vehicles) |

---

## 6. Troubleshooting

| Symptom | Likely cause |
|---|---|
| "Database error: Access denied for user..." | `USER`/`PASSWORD` in `DBConnect.java` don't match your MySQL login - fix in **both** projects and rebuild |
| "Database error: Unknown database 'rental_db'" | Run `database/rental_db.sql` first |
| Web app shows a blank/404 page | Tomcat must be **10.1.x** (Jakarta EE 10). Tomcat 9 or older uses `javax.servlet.*` and will fail to deploy this WAR |
| Desktop app window opens but every screen shows "Database error" | MySQL service isn't running - start it (Windows: Services → MySQL80) |
| Port 8080 already in use | Another Tomcat/app is using it - stop it, or change the port in Tomcat's `conf/server.xml` |
