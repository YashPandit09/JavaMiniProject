<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Server Error - Vehicle Rental System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="text-center">
        <h1 class="display-1 text-muted">500</h1>
        <p class="lead">Something went wrong on our end. Please try again later.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Go Home</a>
    </div>
</body>
</html>
