<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in — Sunrise Dental Clinic</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;12..96,700&family=IBM+Plex+Mono:wght@400;500&family=IBM+Plex+Sans:wght@400;500;600&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="login-page">

    <aside class="login-aside">
        <div>
            <div class="brand-sub">Sunrise Dental Clinic</div>
            <h1>The appointment book, without the double bookings.</h1>
        </div>

        <div>
            <div class="brand-sub" style="margin-bottom:12px">A day at the clinic</div>
            <div class="login-rail" aria-hidden="true">
                <i></i><i class="on"></i><i></i><i class="on"></i><i class="on"></i>
                <i></i><i></i><i class="on"></i><i></i><i class="on"></i>
                <i></i><i></i><i class="on"></i><i></i><i></i>
            </div>
        </div>

        <p>Every appointment gets a number, every slot holds one patient, and every bill prints the same way twice.</p>
    </aside>

    <div class="login-form-wrap">
        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
            <div class="eyebrow">Authorised staff only</div>
            <h2 style="margin-bottom:18px">Sign in</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-error" role="alert">${error}</div>
            </c:if>
            <c:if test="${not empty notice}">
                <div class="alert alert-notice" role="status">${notice}</div>
            </c:if>
            <c:if test="${not empty param.signedout}">
                <div class="alert alert-notice" role="status">You have been signed out.</div>
            </c:if>

            <div class="field">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" value="${username}"
                       autocomplete="username" required autofocus>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       autocomplete="current-password" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width:100%; justify-content:center">Sign in</button>
        </form>
    </div>

</div>
</body>
</html>
