<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Find appointment" activeNav="search">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">Lookup</div>
                <h1>Find an appointment</h1>
            </div>
        </div>

        <section class="card" style="max-width:520px">
            <form method="post" action="${pageContext.request.contextPath}/appointments/search" class="card-body">
                <div class="field">
                    <label for="appointmentNo">Appointment number</label>
                    <input type="text" id="appointmentNo" name="appointmentNo" class="ref"
                           value="${appointmentNo}" placeholder="APT-20260902-001" required autofocus>
                    <div class="hint">Printed at the top of the patient's appointment card.</div>
                </div>
                <button type="submit" class="btn btn-primary">Find appointment</button>
            </form>
        </section>

    </jsp:attribute>
</t:layout>
