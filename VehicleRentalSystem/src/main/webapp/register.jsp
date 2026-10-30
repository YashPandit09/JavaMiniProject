<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register — Vehicle Rental System</title>
    <meta name="description" content="Create a free account to start booking vehicles with the Vehicle Rental System.">
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

        <h2 class="text-center">Create Account</h2>
        <p class="auth-subtitle text-center">Join us and start renting today</p>

        <!-- Error message -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger vr-alert" role="alert">
                <i class="bi bi-exclamation-circle me-1"></i><c:out value="${error}"/>
            </div>
        </c:if>

        <!-- Registration form -->
        <form action="${pageContext.request.contextPath}/register" method="post" novalidate id="registerForm">
            <div class="mb-3">
                <label for="fullName" class="form-label">Full Name</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" id="fullName" name="fullName"
                           placeholder="Your full name" required maxlength="100"
                           value="<c:out value='${fullName}'/>">
                </div>
            </div>

            <div class="mb-3">
                <label for="email" class="form-label">Email Address</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                    <input type="email" class="form-control" id="email" name="email"
                           placeholder="you@example.com" required
                           value="<c:out value='${email}'/>">
                </div>
            </div>

            <div class="mb-3">
                <label for="phone" class="form-label">Phone Number</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                    <input type="tel" class="form-control" id="phone" name="phone"
                           placeholder="10-digit phone number" required pattern="[0-9]{10,15}"
                           value="<c:out value='${phone}'/>">
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password"
                           placeholder="Minimum 6 characters" required minlength="6">
                </div>
            </div>

            <div class="mb-4">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                           placeholder="Re-enter your password" required>
                </div>
                <div id="passwordMismatch" class="text-danger mt-1" style="display:none;font-size:.85rem;">
                    <i class="bi bi-exclamation-circle me-1"></i>Passwords do not match.
                </div>
            </div>

            <button type="submit" class="btn btn-vr-primary w-100 mb-3" id="registerBtn">
                <i class="bi bi-person-plus me-1"></i>Create Account
            </button>
        </form>

        <p class="text-center mb-0">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login" style="font-weight:600;">Sign in</a>
        </p>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Client-side password confirmation check
    document.addEventListener('DOMContentLoaded', function() {
        var pw = document.getElementById('password');
        var cpw = document.getElementById('confirmPassword');
        var msg = document.getElementById('passwordMismatch');

        function check() {
            if (cpw.value && pw.value !== cpw.value) {
                msg.style.display = 'block';
            } else {
                msg.style.display = 'none';
            }
        }
        pw.addEventListener('input', check);
        cpw.addEventListener('input', check);

        document.getElementById('registerForm').addEventListener('submit', function(e) {
            if (pw.value !== cpw.value) {
                e.preventDefault();
                msg.style.display = 'block';
                cpw.focus();
            }
        });
    });
</script>
</body>
</html>
