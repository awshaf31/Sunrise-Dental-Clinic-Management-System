<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="clinic" uri="https://sunrisedental.local/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Reports" activeNav="reports">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">Management reports</div>
                <h1>${displayDate}</h1>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/reports" style="display:flex; gap:8px; align-items:flex-end">
                <div class="field" style="margin:0">
                    <label for="date">Report date</label>
                    <input type="date" id="date" name="date" value="${date}">
                </div>
                <button type="submit" class="btn btn-secondary">Run report</button>
            </form>
        </div>

        <section class="card" style="margin-bottom:18px">
            <div class="card-head">
                <h2>Revenue by treatment</h2>
                <c:if test="${not empty revenueTotal}">
                    <span class="mono">LKR ${clinic:money(revenueTotal)}</span>
                </c:if>
            </div>

            <c:if test="${not empty revenueError}">
                <div class="empty"><p>${revenueError}</p></div>
            </c:if>

            <c:if test="${empty revenueError and empty revenue}">
                <div class="empty"><p>No bills were issued on this day.</p></div>
            </c:if>

            <c:if test="${not empty revenue}">
                <table>
                    <thead>
                    <tr><th>Treatment</th><th class="num">Bills</th><th class="num">Consultation</th><th class="num">Treatment</th><th class="num">Revenue</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${revenue}">
                        <tr>
                            <td>${row.treatmentName}</td>
                            <td class="num">${row.billsIssued}</td>
                            <td class="num">${clinic:money(row.consultationTotal)}</td>
                            <td class="num">${clinic:money(row.treatmentTotal)}</td>
                            <td class="num">${clinic:money(row.revenueTotal)}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="4">Total</td>
                        <td class="num">${clinic:money(revenueTotal)}</td>
                    </tr>
                    </tfoot>
                </table>
            </c:if>
        </section>

        <section class="card">
            <div class="card-head">
                <h2>Appointment schedule</h2>
                <span class="mono">${schedule.size()} appointments</span>
            </div>

            <c:if test="${empty schedule}">
                <div class="empty"><p>No appointments booked for this day.</p></div>
            </c:if>

            <c:if test="${not empty schedule}">
                <table>
                    <thead>
                    <tr><th>Time</th><th>Reference</th><th>Patient</th><th>Dentist</th><th>Treatment</th><th>Status</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="row" items="${schedule}">
                        <tr>
                            <td class="mono">${row.appointmentTime}</td>
                            <td class="mono">${row.appointmentNo}</td>
                            <td>${row.patientName}</td>
                            <td>${row.dentistName}</td>
                            <td>${row.treatmentName}</td>
                            <td><span class="badge badge-${row.status == 'SCHEDULED' ? 'scheduled' : (row.status == 'COMPLETED' ? 'completed' : 'cancelled')}">${row.status}</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </section>

    </jsp:attribute>
</t:layout>
