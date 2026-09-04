<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Problem">
    <jsp:attribute name="body">
        <div class="card" style="max-width:520px">
            <div class="card-body">
                <div class="eyebrow">Something needs attention</div>
                <h1 style="margin:6px 0 10px">${empty title ? 'That did not work' : title}</h1>
                <p>${empty message ? 'Please try again.' : message}</p>
                <div class="actions">
                    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Back to today</a>
                </div>
            </div>
        </div>
    </jsp:attribute>
</t:layout>
