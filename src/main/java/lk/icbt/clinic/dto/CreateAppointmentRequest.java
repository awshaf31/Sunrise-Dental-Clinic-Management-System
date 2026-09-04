package lk.icbt.clinic.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** The shape Gson deserialises a booking request's JSON body into. */
public class CreateAppointmentRequest {
    public String patientName;
    public String address;
    public String contactNumber;
    public Long dentistId;
    public Long treatmentTypeId;
    public LocalDate appointmentDate;
    public LocalTime appointmentTime;
}
