package lk.icbt.clinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A booked visit -- the record the whole system exists to manage.
 * <p>
 * The nested objects (patient, dentist, treatmentType, createdBy) are
 * populated by {@link lk.icbt.clinic.dao.AppointmentDao} through an explicit
 * JOIN, not lazily fetched by a persistence provider -- there isn't one.
 * <p>
 * The database additionally enforces that one dentist cannot hold two
 * appointments in the same slot; see {@code uk_dentist_slot} in the schema.
 */
public class Appointment {

    private Long id;
    private String appointmentNo;
    private Patient patient;
    private Dentist dentist;
    private TreatmentType treatmentType;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    private Staff createdBy;
    private LocalDateTime createdAt;

    public Appointment() {
    }

    public Appointment(String appointmentNo, Patient patient, Dentist dentist,
                        TreatmentType treatmentType, LocalDate appointmentDate,
                        LocalTime appointmentTime, Staff createdBy) {
        this.appointmentNo = appointmentNo;
        this.patient = patient;
        this.dentist = dentist;
        this.treatmentType = treatmentType;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.createdBy = createdBy;
    }

    /** Cancelling is a state change, not a delete: the visit still happened in the record. */
    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Dentist getDentist() { return dentist; }
    public void setDentist(Dentist dentist) { this.dentist = dentist; }

    public TreatmentType getTreatmentType() { return treatmentType; }
    public void setTreatmentType(TreatmentType treatmentType) { this.treatmentType = treatmentType; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public Staff getCreatedBy() { return createdBy; }
    public void setCreatedBy(Staff createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
