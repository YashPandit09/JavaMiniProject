<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${showMyBookings}">My Bookings</c:when>
            <c:when test="${not empty confirmation}">Booking Confirmed</c:when>
            <c:otherwise>Book a Vehicle</c:otherwise>
        </c:choose>
        — Vehicle Rental System
    </title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>

<!-- ===== Navbar ===== -->
<nav class="navbar navbar-expand-lg vr-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">
            <span class="brand-icon"><i class="bi bi-car-front-fill"></i></span>VehicleRental
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav ms-auto align-items-lg-center gap-1">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/vehicles"><i class="bi bi-grid me-1"></i>Vehicles</a></li>
                <c:if test="${not empty sessionScope.userId}">
                    <li class="nav-item"><a class="nav-link ${showMyBookings ? 'active' : ''}" href="${pageContext.request.contextPath}/booking?action=mybookings"><i class="bi bi-journal-check me-1"></i>My Bookings</a></li>
                    <li class="nav-item">
                        <span class="nav-link text-muted"><i class="bi bi-person-circle me-1"></i>
                            <c:out value="${sessionScope.userName}"/>
                        </span>
                    </li>
                    <li class="nav-item"><a class="btn btn-vr-outline btn-sm" href="${pageContext.request.contextPath}/logout">Logout</a></li>
                </c:if>
            </ul>
        </div>
    </div>
</nav>

<!-- ===== Page Header ===== -->
<section style="background:linear-gradient(135deg,var(--vr-primary),#3b82f6);padding:2.5rem 0;color:white;">
    <div class="container">
        <h1 style="font-weight:700;margin-bottom:.25rem;">
            <c:choose>
                <c:when test="${showMyBookings}"><i class="bi bi-journal-check me-2"></i>My Bookings</c:when>
                <c:when test="${not empty confirmation}"><i class="bi bi-check-circle me-2"></i>Booking Confirmed</c:when>
                <c:otherwise><i class="bi bi-calendar-check me-2"></i>Book a Vehicle</c:otherwise>
            </c:choose>
        </h1>
    </div>
</section>

<div class="container py-4">

    <!-- ===== Alerts ===== -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger vr-alert">
            <i class="bi bi-exclamation-circle me-1"></i><c:out value="${error}"/>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success vr-alert">
            <i class="bi bi-check-circle me-1"></i><c:out value="${success}"/>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!--  VIEW 1: Booking Confirmation                                -->
    <!-- ============================================================ -->
    <c:if test="${not empty confirmation}">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="vr-confirmation">
                    <div class="check-icon"><i class="bi bi-check-circle-fill"></i></div>
                    <h3>Booking Confirmed!</h3>
                    <p class="text-muted mb-4">Your vehicle has been reserved successfully.</p>

                    <div class="vr-booking-detail text-start">
                        <div class="row g-3">
                            <div class="col-6">
                                <div class="detail-label">Booking ID</div>
                                <div class="detail-value">#<c:out value="${confirmation.bookingId}"/></div>
                            </div>
                            <div class="col-6">
                                <div class="detail-label">Vehicle</div>
                                <div class="detail-value"><c:out value="${confirmation.vehicleName}"/></div>
                            </div>
                            <div class="col-6">
                                <div class="detail-label">Pickup Date</div>
                                <div class="detail-value"><c:out value="${confirmation.pickupDate}"/></div>
                            </div>
                            <div class="col-6">
                                <div class="detail-label">Return Date</div>
                                <div class="detail-value"><c:out value="${confirmation.returnDate}"/></div>
                            </div>
                            <div class="col-6">
                                <div class="detail-label">Total Days</div>
                                <div class="detail-value"><c:out value="${confirmation.totalDays}"/></div>
                            </div>
                            <div class="col-6">
                                <div class="detail-label">Total Amount</div>
                                <div class="detail-value" style="color:var(--vr-primary);font-size:1.2rem;">
                                    &#8377;<c:out value="${confirmation.totalAmount}"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="mt-4">
                        <a href="${pageContext.request.contextPath}/booking?action=mybookings" class="btn btn-vr-primary me-2">
                            <i class="bi bi-journal-check me-1"></i>View My Bookings
                        </a>
                        <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-outline">
                            <i class="bi bi-grid me-1"></i>Browse More
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!--  VIEW 2: Booking Form                                        -->
    <!-- ============================================================ -->
    <c:if test="${empty confirmation and not showMyBookings and not empty vehicle}">
        <div class="row g-4">
            <!-- Vehicle info column -->
            <div class="col-md-5">
                <div class="vr-booking-detail">
                    <!-- Image or placeholder -->
                    <c:choose>
                        <c:when test="${not empty vehicle.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${vehicle.imageUrl}"
                                 class="img-fluid rounded mb-3" alt="<c:out value='${vehicle.name}'/>"
                                 style="width:100%;height:220px;object-fit:cover;"
                                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                            <div class="vr-img-placeholder rounded mb-3" style="display:none;">
                                <i class="bi bi-car-front"></i>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="vr-img-placeholder rounded mb-3">
                                <c:choose>
                                    <c:when test="${vehicle.vehicleType == 'CAR'}"><i class="bi bi-car-front"></i></c:when>
                                    <c:when test="${vehicle.vehicleType == 'SUV'}"><i class="bi bi-truck"></i></c:when>
                                    <c:otherwise><i class="bi bi-bicycle"></i></c:otherwise>
                                </c:choose>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <h4 style="font-weight:700;"><c:out value="${vehicle.name}"/></h4>
                    <p class="text-muted mb-3">
                        <c:out value="${vehicle.brand}"/> &middot; <c:out value="${vehicle.model}"/>
                    </p>

                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <div class="detail-label">Type</div>
                            <div class="detail-value"><c:out value="${vehicle.vehicleType}"/></div>
                        </div>
                        <div class="col-6">
                            <div class="detail-label">Seats</div>
                            <div class="detail-value"><c:out value="${vehicle.seatingCapacity}"/></div>
                        </div>
                        <div class="col-6">
                            <div class="detail-label">Reg. No.</div>
                            <div class="detail-value"><c:out value="${vehicle.registrationNumber}"/></div>
                        </div>
                        <div class="col-6">
                            <div class="detail-label">Daily Rate</div>
                            <div class="detail-value" style="color:var(--vr-primary);font-size:1.15rem;">
                                &#8377;<c:out value="${vehicle.pricePerDay}"/>
                            </div>
                        </div>
                    </div>

                    <c:if test="${not empty vehicle.description}">
                        <p class="text-muted" style="font-size:.9rem;"><c:out value="${vehicle.description}"/></p>
                    </c:if>
                </div>
            </div>

            <!-- Booking form column -->
            <div class="col-md-7">
                <div class="vr-booking-detail">
                    <h5 style="font-weight:700;margin-bottom:1.25rem;">
                        <i class="bi bi-calendar-plus me-2"></i>Select Your Dates
                    </h5>

                    <form action="${pageContext.request.contextPath}/booking" method="post" id="bookingForm">
                        <input type="hidden" name="vehicleId" value="${vehicle.id}">

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="bookPickup" class="form-label" style="font-weight:600;">Pickup Date</label>
                                <input type="date" class="form-control" id="bookPickup" name="pickupDate"
                                       required value="<c:out value='${pickupDate}'/>">
                            </div>
                            <div class="col-md-6">
                                <label for="bookReturn" class="form-label" style="font-weight:600;">Return Date</label>
                                <input type="date" class="form-control" id="bookReturn" name="returnDate"
                                       required value="<c:out value='${returnDate}'/>">
                            </div>
                        </div>

                        <!-- Price calculator (client-side preview, server calculates the real price) -->
                        <div id="pricePreview" class="mt-4 p-3 rounded" style="background:var(--vr-gray-100);display:none;">
                            <div class="row">
                                <div class="col-6">
                                    <span class="text-muted">Duration</span><br>
                                    <strong id="previewDays">0</strong> day(s)
                                </div>
                                <div class="col-6 text-end">
                                    <span class="text-muted">Estimated Total</span><br>
                                    <strong id="previewTotal" style="font-size:1.25rem;color:var(--vr-primary);">&#8377;0</strong>
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-vr-accent w-100 mt-4" style="padding:.75rem;font-size:1.05rem;">
                            <i class="bi bi-check-circle me-1"></i>Confirm Booking
                        </button>
                    </form>

                    <p class="text-muted text-center mt-3" style="font-size:.8rem;">
                        <i class="bi bi-info-circle me-1"></i>Final amount is calculated on the server.
                        You can cancel a booking before the pickup date.
                    </p>
                </div>
            </div>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!--  VIEW 3: My Bookings List                                    -->
    <!-- ============================================================ -->
    <c:if test="${showMyBookings}">
        <c:choose>
            <c:when test="${not empty bookings}">
                <div class="vr-bookings-table">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Vehicle</th>
                                    <th>Pickup</th>
                                    <th>Return</th>
                                    <th>Days</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${bookings}">
                                    <tr>
                                        <td><strong>#<c:out value="${b.id}"/></strong></td>
                                        <td>
                                            <strong><c:out value="${b.vehicleName}"/></strong><br>
                                            <small class="text-muted"><c:out value="${b.brand}"/> <c:out value="${b.model}"/></small>
                                        </td>
                                        <td><c:out value="${b.pickupDate}"/></td>
                                        <td><c:out value="${b.returnDate}"/></td>
                                        <td><c:out value="${b.totalDays}"/></td>
                                        <td><strong>&#8377;<c:out value="${b.totalAmount}"/></strong></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${b.bookingStatus == 'CONFIRMED'}">
                                                    <span class="badge-confirmed">CONFIRMED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-cancelled">CANCELLED</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${b.canCancel}">
                                                <form action="${pageContext.request.contextPath}/booking" method="post"
                                                      style="display:inline;"
                                                      onsubmit="return confirm('Are you sure you want to cancel booking #${b.id}?');">
                                                    <input type="hidden" name="action" value="cancel">
                                                    <input type="hidden" name="bookingId" value="${b.id}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                                        <i class="bi bi-x-circle me-1"></i>Cancel
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="vr-empty-state">
                    <div class="empty-icon"><i class="bi bi-journal-x"></i></div>
                    <h5>No Bookings Yet</h5>
                    <p>You haven't made any bookings. Browse our vehicles and book your first ride!</p>
                    <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-primary">
                        <i class="bi bi-grid me-1"></i>Browse Vehicles
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

</div>

<!-- ===== Footer ===== -->
<footer class="vr-footer">
    <div class="container">
        <div class="footer-bottom text-center" style="border-top:none;padding-top:0;margin-top:0;">
            &copy; 2026 Vehicle Rental System. All rights reserved.
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var today = new Date().toISOString().split('T')[0];
        var pickup = document.getElementById('bookPickup');
        var ret = document.getElementById('bookReturn');

        if (pickup && ret) {
            pickup.min = today;
            ret.min = today;

            // The price per day is embedded in the vehicle data
            var pricePerDay = ${not empty vehicle ? vehicle.pricePerDay : 0};

            function updatePreview() {
                var p = pickup.value;
                var r = ret.value;
                if (p && r) {
                    var d1 = new Date(p);
                    var d2 = new Date(r);
                    var diff = Math.ceil((d2 - d1) / (1000 * 60 * 60 * 24));
                    if (diff > 0) {
                        document.getElementById('previewDays').textContent = diff;
                        document.getElementById('previewTotal').innerHTML = '&#8377;' + (diff * pricePerDay).toLocaleString('en-IN');
                        document.getElementById('pricePreview').style.display = 'block';
                    } else {
                        document.getElementById('pricePreview').style.display = 'none';
                    }
                } else {
                    document.getElementById('pricePreview').style.display = 'none';
                }
            }

            pickup.addEventListener('change', function() {
                ret.min = this.value;
                updatePreview();
            });
            ret.addEventListener('change', updatePreview);

            // If dates were pre-filled, compute preview
            updatePreview();
        }
    });
</script>
</body>
</html>
