<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="clinic" uri="https://sunrisedental.local/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Book appointment" activeNav="new">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">New booking</div>
                <h1>Book an appointment</h1>
            </div>
        </div>

        <div class="grid grid-rail">

            <section class="card">
                <div class="card-body rail">
                    <div class="rail-head">
                        <span class="rail-title">Their day</span>
                        <span class="rail-count" id="rail-count">Pick a dentist</span>
                    </div>
                    <div class="rail-track" id="rail-track">
                        <c:forEach var="hour" items="${hours}">
                            <div class="slot is-free">
                                <span class="slot-time">${hour}</span>
                                <span class="slot-body"></span>
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
                <div class="card-head"><h2>Patient and visit</h2></div>
                <form method="post" action="${pageContext.request.contextPath}/appointments" class="card-body">

                    <div class="field ${not empty fieldErrors.patientName ? 'has-error' : ''}">
                        <label for="patientName">Patient name</label>
                        <input type="text" id="patientName" name="patientName" value="${patientName}" required>
                        <c:if test="${not empty fieldErrors.patientName}">
                            <div class="field-error">${fieldErrors.patientName}</div>
                        </c:if>
                    </div>

                    <div class="field ${not empty fieldErrors.address ? 'has-error' : ''}">
                        <label for="address">Address</label>
                        <input type="text" id="address" name="address" value="${address}" required>
                        <c:if test="${not empty fieldErrors.address}">
                            <div class="field-error">${fieldErrors.address}</div>
                        </c:if>
                    </div>

                    <div class="field ${not empty fieldErrors.contactNumber ? 'has-error' : ''}">
                        <label for="contactNumber">Contact number</label>
                        <input type="tel" id="contactNumber" name="contactNumber" value="${contactNumber}" required>
                        <div class="hint">Sri Lankan mobile or landline, for example 0771234567</div>
                        <c:if test="${not empty fieldErrors.contactNumber}">
                            <div class="field-error">${fieldErrors.contactNumber}</div>
                        </c:if>
                    </div>

                    <div class="form-row">
                        <div class="field">
                            <label for="dentistId">Dentist</label>
                            <select id="dentistId" name="dentistId" required>
                                <option value="">Select a dentist</option>
                                <c:forEach var="d" items="${dentists}">
                                    <option value="${d.id}" ${dentistId == d.id ? 'selected' : ''}>${d.name} — ${d.specialization}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="treatmentTypeId">Treatment</label>
                            <select id="treatmentTypeId" name="treatmentTypeId" required>
                                <option value="">Select a treatment</option>
                                <c:forEach var="t" items="${treatments}">
                                    <option value="${t.id}" ${treatmentTypeId == t.id ? 'selected' : ''}>${t.name} — LKR ${clinic:money(t.baseFee)}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="field">
                            <label for="appointmentDate">Date</label>
                            <input type="date" id="appointmentDate" name="appointmentDate" value="${appointmentDate}" required>
                        </div>
                        <div class="field">
                            <label for="appointmentTime">Time</label>
                            <select id="appointmentTime" name="appointmentTime" required>
                                <c:forEach var="hour" items="${hours}">
                                    <option value="${hour}" ${appointmentTime == hour ? 'selected' : ''}>${hour}</option>
                                </c:forEach>
                            </select>
                            <div class="hint">The clinic is open 09:00 to 17:00.</div>
                        </div>
                    </div>

                    <div class="actions">
                        <button type="submit" class="btn btn-primary">Book appointment</button>
                        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </section>

        </div>

        <script src="${pageContext.request.contextPath}/js/day-rail.js" defer></script>

    </jsp:attribute>
</t:layout>
