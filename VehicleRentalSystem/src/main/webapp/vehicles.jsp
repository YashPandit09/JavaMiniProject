<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Available Vehicles — Vehicle Rental System</title>
    <meta name="description" content="Browse and search available cars, SUVs, and bikes for rent. Filter by type and availability dates.">
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
                <li class="nav-item"><a class="nav-link active" href="${pageContext.request.contextPath}/vehicles"><i class="bi bi-grid me-1"></i>Vehicles</a></li>
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/booking?action=mybookings"><i class="bi bi-journal-check me-1"></i>My Bookings</a></li>
                        <li class="nav-item">
                            <span class="nav-link text-muted"><i class="bi bi-person-circle me-1"></i>
                                <c:out value="${sessionScope.userName}"/>
                            </span>
                        </li>
                        <li class="nav-item"><a class="btn btn-vr-outline btn-sm" href="${pageContext.request.contextPath}/logout">Logout</a></li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/login"><i class="bi bi-box-arrow-in-right me-1"></i>Login</a></li>
                        <li class="nav-item"><a class="btn btn-vr-primary btn-sm" href="${pageContext.request.contextPath}/register">Register</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<!-- ===== Page Header ===== -->
<section style="background:linear-gradient(135deg,var(--vr-primary),#3b82f6);padding:2.5rem 0;color:white;">
    <div class="container">
        <h1 style="font-weight:700;margin-bottom:.25rem;"><i class="bi bi-grid me-2"></i>Available Vehicles</h1>
        <p style="opacity:.85;margin:0;">Browse our fleet and find the perfect ride for your trip.</p>
    </div>
</section>

<div class="container py-4">

    <!-- ===== Filter Bar ===== -->
    <div class="vr-search-section mb-4" style="margin-top:0;">
        <h5><i class="bi bi-funnel me-2"></i>Filter Vehicles</h5>
        <form action="${pageContext.request.contextPath}/vehicles" method="get">
            <div class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label class="form-label" for="filterPickup">Pickup Date</label>
                    <input type="date" class="form-control" id="filterPickup" name="pickupDate"
                           value="<c:out value='${pickupDate}'/>">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="filterReturn">Return Date</label>
                    <input type="date" class="form-control" id="filterReturn" name="returnDate"
                           value="<c:out value='${returnDate}'/>">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="filterType">Vehicle Type</label>
                    <select class="form-select" id="filterType" name="vehicleType">
                        <option value="">All Types</option>
                        <option value="CAR" ${vehicleType == 'CAR' ? 'selected' : ''}>Car</option>
                        <option value="SUV" ${vehicleType == 'SUV' ? 'selected' : ''}>SUV</option>
                        <option value="BIKE" ${vehicleType == 'BIKE' ? 'selected' : ''}>Bike</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex gap-2">
                    <button type="submit" class="btn btn-vr-primary flex-fill">
                        <i class="bi bi-search me-1"></i>Search
                    </button>
                    <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-outline">
                        <i class="bi bi-x-circle"></i>
                    </a>
                </div>
            </div>
        </form>
    </div>

    <!-- ===== Error message ===== -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger vr-alert">
            <i class="bi bi-exclamation-circle me-1"></i><c:out value="${error}"/>
        </div>
    </c:if>

    <!-- ===== Results count ===== -->
    <c:if test="${not empty vehicles}">
        <p class="text-muted mb-3">
            <strong>${fn:length(vehicles)}</strong> vehicle(s) found
            <c:if test="${not empty vehicleType}"> &middot; Type: <strong><c:out value="${vehicleType}"/></strong></c:if>
            <c:if test="${not empty pickupDate}"> &middot; <c:out value="${pickupDate}"/> to <c:out value="${returnDate}"/></c:if>
        </p>
    </c:if>

    <!-- ===== Vehicle Cards Grid ===== -->
    <c:choose>
        <c:when test="${not empty vehicles}">
            <div class="row g-4">
                <c:forEach var="v" items="${vehicles}">
                    <div class="col-sm-6 col-lg-4">
                        <div class="vr-vehicle-card">
                            <!-- Image or placeholder -->
                            <c:choose>
                                <c:when test="${not empty v.imageUrl}">
                                    <img src="${pageContext.request.contextPath}/${v.imageUrl}"
                                         class="card-img-top" alt="<c:out value='${v.name}'/>"
                                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                                    <div class="vr-img-placeholder" style="display:none;">
                                        <c:choose>
                                            <c:when test="${v.vehicleType == 'CAR'}"><i class="bi bi-car-front"></i></c:when>
                                            <c:when test="${v.vehicleType == 'SUV'}"><i class="bi bi-truck"></i></c:when>
                                            <c:otherwise><i class="bi bi-bicycle"></i></c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="vr-img-placeholder">
                                        <c:choose>
                                            <c:when test="${v.vehicleType == 'CAR'}"><i class="bi bi-car-front"></i></c:when>
                                            <c:when test="${v.vehicleType == 'SUV'}"><i class="bi bi-truck"></i></c:when>
                                            <c:otherwise><i class="bi bi-bicycle"></i></c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="card-body">
                                <div class="vehicle-name"><c:out value="${v.name}"/></div>
                                <div class="vehicle-brand">
                                    <c:out value="${v.brand}"/> &middot; <c:out value="${v.model}"/>
                                </div>

                                <div class="vehicle-meta">
                                    <span class="badge-meta badge-${fn:toLowerCase(v.vehicleType)}">
                                        <c:out value="${v.vehicleType}"/>
                                    </span>
                                    <span class="badge-meta">
                                        <i class="bi bi-people-fill me-1"></i><c:out value="${v.seatingCapacity}"/> Seats
                                    </span>
                                    <span class="badge-meta">
                                        <i class="bi bi-card-text me-1"></i><c:out value="${v.registrationNumber}"/>
                                    </span>
                                </div>

                                <c:if test="${not empty v.description}">
                                    <p class="text-muted" style="font-size:.85rem;margin-bottom:.75rem;">
                                        <c:out value="${v.description}"/>
                                    </p>
                                </c:if>

                                <div class="vehicle-price">
                                    &#8377;<c:out value="${v.pricePerDay}"/> <small>/day</small>
                                </div>

                                <!-- Book Now button — passes dates if the user searched with dates -->
                                <a href="${pageContext.request.contextPath}/booking?vehicleId=${v.id}<c:if test='${not empty pickupDate}'>&pickupDate=${pickupDate}&returnDate=${returnDate}</c:if>"
                                   class="btn btn-vr-primary w-100">
                                    <i class="bi bi-calendar-check me-1"></i>Book Now
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="vr-empty-state">
                <div class="empty-icon"><i class="bi bi-car-front"></i></div>
                <h5>No Vehicles Found</h5>
                <p>Try adjusting your search filters or check back later.</p>
                <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-outline">
                    <i class="bi bi-arrow-counterclockwise me-1"></i>Clear Filters
                </a>
            </div>
        </c:otherwise>
    </c:choose>
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
        var pickup = document.getElementById('filterPickup');
        var ret = document.getElementById('filterReturn');
        if (pickup) pickup.min = today;
        if (ret) ret.min = today;
        if (pickup) {
            pickup.addEventListener('change', function() {
                if (ret) ret.min = this.value;
            });
        }
    });
</script>
</body>
</html>
