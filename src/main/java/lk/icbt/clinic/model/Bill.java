package lk.icbt.clinic.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The bill raised for an appointment.
 * <p>
 * Fees are stored rather than recomputed on demand, so that reprinting a
 * receipt years later shows what the patient actually paid rather than what
 * today's price list would charge.
 */
public class Bill {

    private Long id;
    private Appointment appointment;
    private BigDecimal consultationFee;
    private BigDecimal treatmentFee;
    private BigDecimal totalAmount;
    private LocalDateTime issuedAt;
    private Staff issuedBy;

    public Bill() {
    }

    public Bill(Appointment appointment, BigDecimal consultationFee,
                BigDecimal treatmentFee, Staff issuedBy) {
        this.appointment = appointment;
        this.consultationFee = consultationFee;
        this.treatmentFee = treatmentFee;
        this.totalAmount = consultationFee.add(treatmentFee);
        this.issuedBy = issuedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public BigDecimal getTreatmentFee() { return treatmentFee; }
    public void setTreatmentFee(BigDecimal treatmentFee) { this.treatmentFee = treatmentFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public Staff getIssuedBy() { return issuedBy; }
    public void setIssuedBy(Staff issuedBy) { this.issuedBy = issuedBy; }
}
