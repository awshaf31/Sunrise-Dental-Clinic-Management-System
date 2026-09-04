package lk.icbt.clinic.service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Everything needed to book a visit, as the service layer wants it.
 * <p>
 * Kept separate from whatever the servlet parses from the HTTP request, so a
 * change to the request format never reaches into the business rules.
 */
public record BookingRequest(
        String patientName,
        String address,
        String contactNumber,
        Long dentistId,
        Long treatmentTypeId,
        LocalDate appointmentDate,
        LocalTime appointmentTime) {
}
