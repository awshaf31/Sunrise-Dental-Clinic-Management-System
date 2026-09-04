<%--
  Shared page frame: sidebar, flash messages, footer wiring.

  A JSP tag file rather than a Thymeleaf fragment -- this is the JSP
  platform's own include/composition mechanism, used the same way a
  <jsp:include> would be, just parameterised. No templating framework sits
  underneath it.
--%>
<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="activeNav" required="false" %>
<%@ attribute name="body" fragment="true" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} — Sunrise Dental Clinic</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;12..96,700&family=IBM+Plex+Mono:wght@400;500&family=IBM+Plex+Sans:wght@400;500;600&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<div class="app">

    <nav class="sidebar">
        <div class="brand">
            <div class="brand-mark">Sunrise<br>Dental Clinic</div>
            <div class="brand-sub">Reception desk</div>
        </div>

        <ul class="menu">
            <li><a href="${pageContext.request.contextPath}/" ${activeNav == 'today' ? 'aria-current="page"' : ''}>
                <span class="num">01</span> Today</a></li>
            <li><a href="${pageContext.request.contextPath}/appointments/new" ${activeNav == 'new' ? 'aria-current="page"' : ''}>
                <span class="num">02</span> Book appointment</a></li>
            <li><a href="${pageContext.request.contextPath}/appointments/search" ${activeNav == 'search' ? 'aria-current="page"' : ''}>
                <span class="num">03</span> Find appointment</a></li>
            <li><a href="${pageContext.request.contextPath}/reports" ${activeNav == 'reports' ? 'aria-current="page"' : ''}>
                <span class="num">04</span> Reports</a></li>
            <li><a href="${pageContext.request.contextPath}/help" ${activeNav == 'help' ? 'aria-current="page"' : ''}>
                <span class="num">05</span> Help</a></li>
        </ul>

        <c:if test="${not empty sessionScope['clinic.user']}">
            <div class="who">
                <div class="who-name">${sessionScope['clinic.user'].fullName}</div>
                <div class="who-role">${sessionScope['clinic.user'].role}</div>
                <a href="${pageContext.request.contextPath}/logout">Sign out</a>
            </div>
        </c:if>
    </nav>

    <main class="main">
        <c:if test="${not empty sessionScope['flash.success']}">
            <div class="alert alert-success" role="status">${sessionScope['flash.success']}</div>
            <c:remove var="flash.success" scope="session"/>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert">${error}</div>
        </c:if>
        <c:if test="${not empty notice}">
            <div class="alert alert-notice" role="status">${notice}</div>
        </c:if>

        <jsp:invoke fragment="body"/>
    </main>

</div>
</body>
</html>
