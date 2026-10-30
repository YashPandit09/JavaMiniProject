# Viva Notes

One-line pointer from each Advanced Java concept (slide 6 of the PPT) to the
actual file, plus likely questions with short answers. File paths are
relative to the project root; the desktop app mirrors the same DAO/model
classes under `VehicleRentalDesktop/src/main/java/com/rental/`.

## 1. Concept -> file map

| Concept | Where to show it |
|---|---|
| Classes, objects, encapsulation | `VehicleRentalWeb/.../model/Customer.java`, `Vehicle.java`, `Booking.java`, `User.java` - private fields, getters/setters |
| Inheritance and overriding | Any servlet, e.g. `servlet/AuthSer.java` - `extends HttpServlet`, overrides `doGet()` / `doPost()` |
| User-defined exceptions | `exception/BookingException.java` (declaration) + `dao/BookingDAO.java` method `book()` (try/catch/throw) |
| Collections, lambda, streams | `dao/VehicleDAO.java` returns `ArrayList<Vehicle>`; `servlet/DashboardSer.java` - `vehicles.stream().filter(v -> v.isAvailable() && ...).count()` |
| JDBC | `util/DBConnect.java` (`DriverManager.getConnection`), every DAO uses `PreparedStatement` with `?` placeholders |
| JDBC transactions | `dao/BookingDAO.java` methods `book()`, `cancel()`, `returnVehicle()` - `setAutoCommit(false)`, `commit()`, `rollback()` |
| Servlets, forwarding, sessions | `servlet/AuthSer.java` (`HttpSession`), `servlet/BookSer.java` (`setAttribute` + `RequestDispatcher.forward`), `servlet/ResultSer.java` (reads the attributes) |
| Layered design | Browser -> Servlet (`servlet/`) -> DAO (`dao/`) -> MySQL, one shared `DBConnect` |
| *(extra, desktop app)* Multithreading | `ui/*Panel.java` - every DB call runs inside a `SwingWorker` so the Swing UI thread never blocks |
| *(extra, desktop app)* GUI layout | `ui/MainFrame.java` - `CardLayout` switches between the 7 screens without opening new windows |

## 2. Likely questions and answers

**Q: Why `PreparedStatement` instead of `Statement`?**
Values are bound with `?` placeholders and sent separately from the SQL
text, so user input can never be interpreted as SQL - prevents SQL
injection. It's also pre-compiled by MySQL, so repeated calls are faster.

**Q: How exactly does the system stop a double booking?**
`BookingDAO.book()` runs one transaction: it `SELECT ... FOR UPDATE`s the
vehicle row (locks it), then checks `COUNT(*)` of existing `BOOKED` rows for
that vehicle where `from_date <= newTo AND GREATEST(to_date, today) >= newFrom`.
If that count is `> 0` it throws `BookingException` and the insert never
happens. The row lock means a second, simultaneous booking attempt for the
same vehicle has to wait for the first transaction to commit, so it then
sees the just-inserted row and correctly gets rejected. This was verified
by firing 10 simultaneous booking requests at the same vehicle/dates -
exactly 1 was confirmed, 9 were rejected (see `docs/test-results.md`, X21).

**Q: What does `FOR UPDATE` do?**
It takes a row-level lock on the selected row for the rest of the
transaction. Any other transaction trying to `SELECT ... FOR UPDATE` (or
write) the same row has to wait until this transaction commits or rolls
back. This is what makes the overlap check safe under concurrency.

**Q: Why is `GREATEST(b.to_date, ?)` in the overlap query, instead of just `b.to_date`?**
To treat an **overdue** booking (return date already passed, vehicle not
actually returned yet) as still busy through today, not just through its
original due date. Without it, an overdue vehicle could be double-booked
starting "today" even though nobody has brought it back.

**Q: What happens if something fails partway through a booking?**
`catch (SQLException | BookingException e) { con.rollback(); throw e; }` -
any failure undoes every change made so far in that transaction (nothing
partially saved), and the error propagates up to the servlet/UI, which
shows the message to the user. The `finally { con.close(); }` always
releases the connection.

**Q: Difference between `sendRedirect()` and `forward()`?**
`sendRedirect()` sends a new HTTP response telling the browser to make a
fresh request to a new URL (URL changes, old `request` data is lost) - used
after login/logout. `forward()` passes the same `request`/`response` to
another servlet on the server side, so `setAttribute()` data survives and
the browser URL doesn't change - used to hand off to `ResultSer` for the
success/error page.

**Q: Why a `BookingException` instead of just using `SQLException`?**
It separates **business rule failures** (dates invalid, vehicle not
available, no licence - expected, user-correctable) from **database/
infrastructure failures** (`SQLException` - connection lost, etc.). The
servlet/UI shows a different kind of message for each.

**Q: Why have a DAO layer instead of writing SQL directly in the servlet?**
Separation of concerns: the servlet only reads form data and decides what
HTML/response to send; the DAO only knows how to talk to the database. The
same DAO classes are reused unchanged by the desktop app - proof that the
layering actually pays off.

**Q: How is "only ADMIN can add or edit vehicles" enforced?**
Checked server-side in `VehicleSer` on both `doGet()` (viewing the add/edit
form) and `doPost()` (saving) - never trust the client. The desktop app's
`VehiclePanel` checks `user.isAdmin()` before opening the same form dialog.

**Q: Why `SwingWorker` in the desktop app?**
Swing is single-threaded (the Event Dispatch Thread draws the UI and
handles clicks). A JDBC call can take noticeable time; running it directly
on that thread would freeze the whole window. `SwingWorker.doInBackground()`
runs the DB call on a separate thread, and `done()` runs back on the EDT to
safely update the UI once the result is ready.

**Q: How do the web app and desktop app stay in sync?**
They aren't synced with each other directly - both simply read and write
the same MySQL database (`rental_db`) through the same DAO logic, with no
caching in either app. A booking made in one is visible in the other the
next time that screen is refreshed/reopened.

**Q: What's the one limitation you'd fix first for real use?**
Passwords are stored in plain text in `users.password` (visible on the
Limitations slide). The fix is to hash them (e.g. BCrypt) on save and
compare hashes on login, never storing or comparing the raw password.

**Q: Why does `returnVehicle()` charge a minimum of 1 day even for a same-day return?**
`long days = ... ; if (days < 1) days = 1;` - matches how rental pricing
normally works (daily rate, rounded up, minimum one day charged), and
avoids a Rs. 0.00 bill if a vehicle is picked up and returned the same day.
