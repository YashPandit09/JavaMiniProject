<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Vehicle Rental System</title>
    <meta name="description" content="Log in to your Vehicle Rental System account to book vehicles and manage your bookings.">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>

<div class="vr-auth-wrapper">
    <div class="vr-auth-card">
        <!-- Logo / Back link -->
        <div class="text-center mb-4">
            <a href="${pageContext.request.contextPath}/" class="text-decoration-none">
                <span style="font-size:2rem;color:var(--vr-primary);"><i class="bi bi-car-front-fill"></i></span>
                <div style="font-weight:700;font-size:1.25rem;color:var(--vr-gray-900);">VehicleRental</div>
            </a>
        </div>

        <h2 class="text-center">Welcome Back</h2>
        <p class="auth-subtitle text-center">Sign in to your account</p>

        <!-- Success messages -->
        <c:if test="${param.registered == 'true'}">
            <div class="alert alert-success vr-alert" role="alert">
                <i class="bi bi-check-circle me-1"></i>Registration successful! Please log in.
            </div>
        </c:if>
        <c:if test="${param.loggedout == 'true'}">
            <div class="alert alert-info vr-alert" role="alert">
                <i class="bi bi-box-arrow-right me-1"></i>You have been logged out.
            </div>
        </c:if>
        <c:if test="${param.redirected == 'true'}">
            <div class="alert alert-warning vr-alert" role="alert">
                <i class="bi bi-exclamation-triangle me-1"></i>Please log in to continue.
            </div>
        </c:if>

        <!-- Error message -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger vr-alert" role="alert">
                <i class="bi bi-exclamation-circle me-1"></i><c:out value="${error}"/>
            </div>
        </c:if>

        <!-- Login form -->
        <form action="${pageContext.request.contextPath}/login" method="post" novalidate>
            <div class="mb-3">
                <label for="email" class="form-label">Email Address</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                    <input type="email" class="form-control" id="email" name="email"
                           placeholder="you@example.com" required
                           value="<c:out value='${email}'/>">
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password"
                           placeholder="Enter your password" required>
                </div>
            </div>

            <button type="submit" class="btn btn-vr-primary w-100 mb-3">
                <i class="bi bi-box-arrow-in-right me-1"></i>Sign In
            </button>
        </form>

        <p class="text-center mb-0">
            Don't have an account?
            <a href="${pageContext.request.contextPath}/register" style="font-weight:600;">Create one</a>
        </p>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
