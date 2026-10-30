# Functional Test Results

**Environment:** Tomcat 10.1.59 + MySQL 8.0.45, Windows 11
**Run date:** 26 Oct 2026
**Result: 10 / 10 passed** (slide 8 cases) + 32 additional checks, all passing

How these were run: an automated `curl` script drove the web app's servlets
directly (login, book, cancel, return, register, add vehicle, etc.) and
checked each response for the expected message. The same business rules
(`BookingDAO`, `CustomerDAO`, `VehicleDAO`) are shared by the desktop app, so
a manual click-through of the desktop app's Booking and Return & Billing
screens was used to confirm the identical transaction logic there (see
"Desktop app spot-check" below).

## Slide 8 test cases

| ID | Test case | Expected result | Status |
|---|---|---|---|
| T01 | Open dashboard without login | Redirected to login.html | **PASS** |
| T02 | Log in with a wrong password | "Invalid username / password" | **PASS** |
| T03 | Phone number with 9 digits | "Phone must be a 10-digit number" | **PASS** |
| T04 | Book an available vehicle | "Booking #n confirmed" | **PASS** |
| T05 | Overlapping dates, same vehicle | "Vehicle not available" | **PASS** |
| T06 | Return date before pick-up date | "Return date must be after pick-up date" | **PASS** |
| T07 | Book a vehicle in MAINTENANCE | "Vehicle not available" | **PASS** |
| T08 | Staff opens the add-vehicle page | "Only ADMIN can add or edit vehicles" | **PASS** |
| T09 | Cancel an already cancelled booking | "Booking is already CANCELLED" | **PASS** |
| T10 | Return a vehicle that is not rented | "No active booking for this vehicle" | **PASS** |

## Additional checks (web app)

| # | Test case | Result |
|---|---|---|
| X01 | Password is case-sensitive | PASS |
| X02 | Admin login redirects to dashboard | PASS |
| X03 | Booking total = days x rate (2 x Rs.1,500 = Rs.3,000) | PASS |
| X04 | Pick-up date in the past is rejected | PASS |
| X05 | Customer with no licence number cannot book | PASS |
| X06 | A vehicle still out (overdue, not yet returned) can't be re-booked for today | PASS |
| X07 | Returning a vehicle on rent closes the booking and shows the bill | PASS |
| X08 | Bill = days x rate (2 x Rs.1,800 = Rs.3,600) | PASS |
| X09 | Returning an overdue vehicle shows "Late by N day(s)" | PASS |
| X10 | A rental that already started cannot be cancelled (must be returned instead) | PASS |
| X11 | An upcoming (not yet started) booking can be cancelled | PASS |
| X12 | Registering a customer with valid data succeeds | PASS |
| X13 | Licence number is cleaned to upper-case, spaces/dashes stripped | PASS |
| X14 | Typed HTML in a name field is escaped, not executed (XSS-safe) | PASS |
| X15 | Duplicate licence number is rejected | PASS |
| X16 | STAFF cannot save a vehicle even by posting the form directly | PASS |
| X17 | ADMIN can add a vehicle; registration number is cleaned to upper-case | PASS |
| X18 | Duplicate registration number is rejected | PASS |
| X19 | Non-numeric rate per day is rejected | PASS |
| X20 | Free-vehicle search correctly excludes an already-booked vehicle | PASS |
| X21 | **Concurrency:** 10 simultaneous booking requests for the same vehicle/dates -> exactly 1 confirmed, 9 rejected (row-lock proven) | PASS |
| X22 | After logout, the dashboard redirects back to login | PASS |
| - | All 10 pages load with no "Database error" while logged in | PASS |

## Desktop app spot-check (manual, 28 Oct 2026)

| Screen | Action | Result |
|---|---|---|
| Login | admin / admin123 | Logged in, MainFrame opened |
| Dashboard | Loads live stats | Matched the web app's numbers for the same database |
| Customers | List shows a customer registered earlier through the **web app** | Confirms both apps share one database |
| Vehicles | List shows a vehicle added earlier through the **web app** | Confirms shared database |
| New Booking | Search free vehicles, then confirm a booking | "Booking #17 confirmed" - saved via the same `BookingDAO.book()` transaction |
| Return & Billing | Return the vehicle just booked | Bill correctly computed as 1 x rate, payment saved |
| Reports | Revenue, booking counts, payments | Updated immediately to include the booking/payment just made |

## Known limitation found during testing

None of the above failed. The one pre-existing limitation (documented on the
PPT's Limitations slide) is that passwords are stored in plain text in the
`users` table - acceptable for a classroom demo, called out explicitly as
future work (password hashing).
