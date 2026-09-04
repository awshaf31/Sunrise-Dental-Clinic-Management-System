package lk.icbt.clinic.dto;

import java.time.LocalTime;

/** One line of the day's schedule, for the reception desk's daily list. */
public record DailyScheduleRow(
        LocalTime appointmentTime, String appointmentNo, String patientName,
        String contactNumber, String dentistName, String treatmentName, String status) {
}
