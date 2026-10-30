<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vehicle Rental System — Rent Your Ride</title>
    <meta name="description" content="Online Vehicle Rental Management System. Browse cars, SUVs, and bikes. Book a vehicle for your next trip at affordable daily rates.">
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
                <li class="nav-item"><a class="nav-link active" href="${pageContext.request.contextPath}/"><i class="bi bi-house-door me-1"></i>Home</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/vehicles"><i class="bi bi-grid me-1"></i>Vehicles</a></li>
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

<!-- ===== Hero Section ===== -->
<section class="vr-hero">
    <div class="container position-relative" style="z-index:5;">
        <div class="row align-items-center">
            <div class="col-lg-7">
                <h1>Find Your Perfect<br>Ride Today</h1>
                <p class="lead mb-4">Browse our wide selection of cars, SUVs, and bikes. Affordable daily rates, easy online booking, and a seamless rental experience.</p>
                <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-accent me-2">
                    <i class="bi bi-search me-1"></i>Browse Vehicles
                </a>
                <c:if test="${empty sessionScope.userId}">
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-light">
                        <i class="bi bi-person-plus me-1"></i>Create Account
                    </a>
                </c:if>
            </div>
            <div class="col-lg-5 text-center mt-4 mt-lg-0">
                <i class="bi bi-car-front" style="font-size:12rem;opacity:.15;"></i>
            </div>
        </div>
    </div>
</section>

<!-- ===== Search Section ===== -->
<div class="container">
    <div class="vr-search-section">
        <h5><i class="bi bi-funnel me-2"></i>Search Available Vehicles</h5>
        <form action="${pageContext.request.contextPath}/vehicles" method="get">
            <div class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label class="form-label" for="searchPickup">Pickup Date</label>
                    <input type="date" class="form-control" id="searchPickup" name="pickupDate">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="searchReturn">Return Date</label>
                    <input type="date" class="form-control" id="searchReturn" name="returnDate">
                </div>
                <div class="col-md-3">
                    <label class="form-label" for="searchType">Vehicle Type</label>
                    <select class="form-select" id="searchType" name="vehicleType">
                        <option value="">All Types</option>
                        <option value="CAR">Car</option>
                        <option value="SUV">SUV</option>
                        <option value="BIKE">Bike</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-vr-primary w-100">
                        <i class="bi bi-search me-1"></i>Search
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- ===== Why Choose Us ===== -->
<section class="py-5 mt-4">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="vr-section-title">Why Choose Us</h2>
            <p class="vr-section-subtitle">We make vehicle rental simple, affordable, and reliable.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-4">
                <div class="text-center p-4">
                    <div class="mb-3"><i class="bi bi-shield-check" style="font-size:2.5rem;color:var(--vr-primary);"></i></div>
                    <h5 style="font-weight:700;">Verified Vehicles</h5>
                    <p class="text-muted">All vehicles are regularly serviced and inspected for your safety.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="text-center p-4">
                    <div class="mb-3"><i class="bi bi-currency-rupee" style="font-size:2.5rem;color:var(--vr-accent);"></i></div>
                    <h5 style="font-weight:700;">Best Prices</h5>
                    <p class="text-muted">Transparent daily rates with no hidden charges. Pay only for the days you ride.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="text-center p-4">
                    <div class="mb-3"><i class="bi bi-lightning-charge" style="font-size:2.5rem;color:var(--vr-success);"></i></div>
                    <h5 style="font-weight:700;">Instant Booking</h5>
                    <p class="text-muted">Book in seconds. Pick your dates, select a vehicle, and you're good to go.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ===== Featured Vehicles (static showcase with CTA) ===== -->
<section class="py-5" style="background:var(--vr-gray-100);">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="vr-section-title">Popular Vehicles</h2>
            <p class="vr-section-subtitle">Our most booked rides. Click below to see the full fleet.</p>
        </div>
        <div class="row g-4">
            <!-- Card 1 -->
            <div class="col-md-4">
                <div class="vr-vehicle-card">
                    <div class="vr-img-placeholder"><i class="bi bi-car-front"></i></div>
                    <div class="card-body">
                        <div class="vehicle-name">Honda City</div>
                        <div class="vehicle-brand">Honda &middot; City V CVT</div>
                        <div class="vehicle-meta">
                            <span class="badge-meta badge-car">CAR</span>
                            <span class="badge-meta"><i class="bi bi-people-fill me-1"></i>5 Seats</span>
                        </div>
                        <div class="vehicle-price">&#8377;2,500 <small>/day</small></div>
                        <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-primary w-100">View Details</a>
                    </div>
                </div>
            </div>
            <!-- Card 2 -->
            <div class="col-md-4">
                <div class="vr-vehicle-card">
                    <div class="vr-img-placeholder"><i class="bi bi-truck"></i></div>
                    <div class="card-body">
                        <div class="vehicle-name">Mahindra XUV700</div>
                        <div class="vehicle-brand">Mahindra &middot; XUV700 AX7</div>
                        <div class="vehicle-meta">
                            <span class="badge-meta badge-suv">SUV</span>
                            <span class="badge-meta"><i class="bi bi-people-fill me-1"></i>7 Seats</span>
                        </div>
                        <div class="vehicle-price">&#8377;4,000 <small>/day</small></div>
                        <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-primary w-100">View Details</a>
                    </div>
                </div>
            </div>
            <!-- Card 3 -->
            <div class="col-md-4">
                <div class="vr-vehicle-card">
                    <div class="vr-img-placeholder"><i class="bi bi-bicycle"></i></div>
                    <div class="card-body">
                        <div class="vehicle-name">Royal Enfield Classic 350</div>
                        <div class="vehicle-brand">Royal Enfield &middot; Classic 350</div>
                        <div class="vehicle-meta">
                            <span class="badge-meta badge-bike">BIKE</span>
                            <span class="badge-meta"><i class="bi bi-people-fill me-1"></i>2 Seats</span>
                        </div>
                        <div class="vehicle-price">&#8377;900 <small>/day</small></div>
                        <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-primary w-100">View Details</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="text-center mt-4">
            <a href="${pageContext.request.contextPath}/vehicles" class="btn btn-vr-accent btn-lg">
                <i class="bi bi-grid me-1"></i>Browse All Vehicles
            </a>
        </div>
    </div>
</section>

<!-- ===== Footer ===== -->
<footer class="vr-footer">
    <div class="container">
        <div class="row g-4">
            <div class="col-md-4">
                <h5><i class="bi bi-car-front-fill me-2" style="color:var(--vr-accent);"></i>VehicleRental</h5>
                <p>Your trusted partner for car, SUV, and bike rentals. Book online, hit the road.</p>
            </div>
            <div class="col-md-4">
                <h5>Quick Links</h5>
                <ul class="list-unstyled">
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/"><i class="bi bi-chevron-right me-1"></i>Home</a></li>
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/vehicles"><i class="bi bi-chevron-right me-1"></i>Vehicles</a></li>
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/login"><i class="bi bi-chevron-right me-1"></i>Login</a></li>
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/register"><i class="bi bi-chevron-right me-1"></i>Register</a></li>
                </ul>
            </div>
            <div class="col-md-4">
                <h5>Contact</h5>
                <ul class="list-unstyled">
                    <li class="mb-2"><i class="bi bi-geo-alt me-2"></i>Pune, Maharashtra, India</li>
                    <li class="mb-2"><i class="bi bi-telephone me-2"></i>+91 98765 43210</li>
                    <li class="mb-2"><i class="bi bi-envelope me-2"></i>info@vehiclerental.com</li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom text-center">
            &copy; 2026 Vehicle Rental System. All rights reserved. | Mini Project — Advanced Java Programming
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Set minimum date on search inputs to today
    document.addEventListener('DOMContentLoaded', function() {
        var today = new Date().toISOString().split('T')[0];
        var pickup = document.getElementById('searchPickup');
        var ret = document.getElementById('searchReturn');
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
