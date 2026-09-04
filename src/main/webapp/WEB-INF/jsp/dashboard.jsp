<%--
  Dates are formatted server-side in the servlet, not with <fmt:formatDate> --
  JSTL's formatting tags expect java.util.Date and have no built-in support
  for java.time.LocalDate, which is what the service layer uses throughout.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Today" activeNav="today">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">Day book</div>
                <h1>${displayDate}</h1>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/" style="display:flex; gap:8px; align-items:flex-end">
                <div class="field" style="margin:0">
                    <label for="date">Show another day</label>
                    <input type="date" id="date" name="date" value="${date}">
                </div>
                <button type="submit" class="btn btn-secondary">Go</button>
            </form>
        </div>

        <div class="grid grid-rail">

            <section class="card">
                <div class="card-body rail">
                    <div class="rail-head">
                        <span class="rail-title">Chair time</span>
                        <span class="rail-count">${bookedCount} booked</span>
                    </div>

                    <div class="rail-track">
                        <c:forEach var="slot" items="${slots}">
                            <%-- EL's RecordELResolver only resolves a record's actual
                                 components (time, patientName) to property access; a
                                 handwritten method like isTaken() needs to be called
                                 explicitly, not accessed as if it were a component. --%>
                            <div class="slot ${slot.isTaken() ? 'is-taken' : 'is-free'}">
                                <span class="slot-time">${slot.time}</span>
                                <span class="slot-body">${slot.patientName}</span>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="rail-legend">
                        <span><i class="swatch free"></i> Free</span>
                        <span><i class="swatch taken"></i> Booked</span>
                    </div>
                </div>
            </section>

            <section class="card">
                <div class="card-head">
                    <h2>Appointments</h2>
                    <div class="stat-row">
                        <div>
                            <div class="stat-value">${bookedCount}</div>
                            <div class="stat-label">Booked</div>
                        </div>
                        <c:if test="${cancelledCount > 0}">
                            <div>
                                <div class="stat-value" style="color:var(--rose)">${cancelledCount}</div>
                                <div class="stat-label">Cancelled</div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty schedule}">
                        <div class="empty">
                            <p>No appointments booked for this day.</p>
                            <a href="${pageContext.request.contextPath}/appointments/new" class="btn btn-primary">Book an appointment</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table>
                            <thead>
                            <tr><th>Time</th><th>Patient</th><th>Dentist</th><th>Treatment</th><th>Status</th><th></th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${schedule}">
                                <tr>
                                    <td class="mono">${row.appointmentTime}</td>
                                    <td>
                                        <div>${row.patientName}</div>
                                        <div class="mono" style="font-size:.76rem; color:var(--ink-3)">${row.contactNumber}</div>
                                    </td>
                                    <td>${row.dentistName}</td>
                                    <td>${row.treatmentName}</td>
                                    <td>
                                        <span class="badge badge-${row.status == 'SCHEDULED' ? 'scheduled' : (row.status == 'COMPLETED' ? 'completed' : 'cancelled')}">${row.status}</span>
                                    </td>
                                    <td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/appointments/${row.appointmentNo}">Open</a></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </section>

        </div>

    </jsp:attribute>
</t:layout>
