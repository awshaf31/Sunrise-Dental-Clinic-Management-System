package lk.icbt.clinic.dto;

import lk.icbt.clinic.model.Appointment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An appointment flattened for the wire -- the names the user needs rather
 * than the object graph the database stores. Built by hand from the domain
 * object, since there is no framework to map one to the other automatically.
 */
public class AppointmentResponse {
    public final String appointmentNo;
    public final String patientNo;
    public final String patientName;
    public final String address;
    public final String contactNumber;
    public final String dentistName;
    public final String treatmentCode;
    public final String treatmentName;
    public final BigDecimal treatmentBaseFee;
    public final LocalDate appointmentDate;
    public final LocalTime appointmentTime;
    public final String status;

    public AppointmentResponse(Appointment a) {
        this.appointmentNo = a.getAppointmentNo();
        this.patientNo = a.getPatient().getPatientNo();
        this.patientName = a.getPatient().getName();
        this.address = a.getPatient().getAddress();
        this.contactNumber = a.getPatient().getContactNumber();
        this.dentistName = a.getDentist().getName();
        this.treatmentCode = a.getTreatmentType().getCode().name();
        this.treatmentName = a.getTreatmentType().getName();
        this.treatmentBaseFee = a.getTreatmentType().getBaseFee();
        this.appointmentDate = a.getAppointmentDate();
        this.appointmentTime = a.getAppointmentTime();
        this.status = a.getStatus().name();
    }
}
