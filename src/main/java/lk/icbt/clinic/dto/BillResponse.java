package lk.icbt.clinic.dto;

import lk.icbt.clinic.model.Bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Everything the receipt needs to print, in one object. */
public class BillResponse {
    public final Long billId;
    public final String appointmentNo;
    public final String patientName;
    public final String dentistName;
    public final String treatmentName;
    public final BigDecimal consultationFee;
    public final BigDecimal treatmentFee;
    public final BigDecimal totalAmount;
    public final LocalDateTime issuedAt;
    public final String issuedBy;

    public BillResponse(Bill b) {
        this.billId = b.getId();
        this.appointmentNo = b.getAppointment().getAppointmentNo();
        this.patientName = b.getAppointment().getPatient().getName();
        this.dentistName = b.getAppointment().getDentist().getName();
        this.treatmentName = b.getAppointment().getTreatmentType().getName();
        this.consultationFee = b.getConsultationFee();
        this.treatmentFee = b.getTreatmentFee();
        this.totalAmount = b.getTotalAmount();
        this.issuedAt = b.getIssuedAt();
        this.issuedBy = b.getIssuedBy().getFullName();
    }
}
