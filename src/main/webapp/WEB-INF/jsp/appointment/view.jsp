<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Appointment" activeNav="">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">Appointment</div>
                <h1 class="ref">${appointment.appointmentNo}</h1>
            </div>
            <span class="badge badge-${appointment.status == 'SCHEDULED' ? 'scheduled' : (appointment.status == 'COMPLETED' ? 'completed' : 'cancelled')}">${appointment.status}</span>
        </div>

        <div class="grid grid-2">

            <section class="card">
                <div class="card-head"><h2>Patient</h2></div>
                <div class="card-body">
                    <dl class="detail">
                        <dt>Name</dt><dd>${appointment.patient.name}</dd>
                        <dt>Patient number</dt><dd class="mono">${appointment.patient.patientNo}</dd>
                        <dt>Address</dt><dd>${appointment.patient.address}</dd>
                        <dt>Contact</dt><dd class="mono">${appointment.patient.contactNumber}</dd>
                    </dl>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Visit</h2></div>
                <div class="card-body">
                    <dl class="detail">
                        <dt>Dentist</dt><dd>${appointment.dentist.name}</dd>
                        <dt>Treatment</dt><dd>${appointment.treatmentType.name}</dd>
                        <dt>Date</dt><dd class="mono">${appointment.appointmentDate}</dd>
                        <dt>Time</dt><dd class="mono">${appointment.appointmentTime}</dd>
                    </dl>
                </div>
            </section>

        </div>

        <div class="actions">
            <form method="post" action="${pageContext.request.contextPath}/bills/${appointment.appointmentNo}" style="display:inline">
                <button type="submit" class="btn btn-primary">Prepare bill</button>
            </form>
            <c:if test="${appointment.status == 'SCHEDULED'}">
                <form method="post" action="${pageContext.request.contextPath}/appointments/${appointment.appointmentNo}/cancel"
                      style="display:inline" onsubmit="return confirm('Cancel this appointment? The slot will be released for other patients.')">
                    <button type="submit" class="btn btn-danger">Cancel appointment</button>
                </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Back to today</a>
        </div>

    </jsp:attribute>
</t:layout>
