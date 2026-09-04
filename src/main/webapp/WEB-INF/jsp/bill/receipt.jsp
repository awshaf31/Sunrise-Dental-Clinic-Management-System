<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="clinic" uri="https://sunrisedental.local/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Bill">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">Receipt</div>
                <h1>Patient bill</h1>
            </div>
        </div>

        <div class="receipt">
            <div class="receipt-head">
                <h2>Sunrise Dental Clinic</h2>
                <span class="mono">Colombo</span>
                <span class="mono">${bill.appointment.appointmentNo}</span>
            </div>

            <dl class="detail" style="grid-template-columns:110px 1fr">
                <dt>Patient</dt><dd>${bill.appointment.patient.name}</dd>
                <dt>Dentist</dt><dd>${bill.appointment.dentist.name}</dd>
                <dt>Issued</dt><dd class="mono">${bill.issuedAt}</dd>
            </dl>

            <div class="receipt-lines">
                <div class="receipt-line">
                    <span>Consultation</span>
                    <span class="amount">${clinic:money(bill.consultationFee)}</span>
                </div>
                <div class="receipt-line">
                    <span>${bill.appointment.treatmentType.name}</span>
                    <span class="amount">${clinic:money(bill.treatmentFee)}</span>
                </div>
            </div>

            <div class="receipt-total">
                <span>Total (LKR)</span>
                <span class="amount">${clinic:money(bill.totalAmount)}</span>
            </div>

            <div class="receipt-foot">
                <div>Issued by ${bill.issuedBy.fullName}</div>
                <div style="margin-top:6px">Thank you for visiting Sunrise Dental Clinic.</div>
            </div>
        </div>

        <div class="actions no-print">
            <button type="button" class="btn btn-primary" onclick="window.print()">Print bill</button>
            <a href="${pageContext.request.contextPath}/appointments/${bill.appointment.appointmentNo}" class="btn btn-secondary">Back to appointment</a>
        </div>

    </jsp:attribute>
</t:layout>
