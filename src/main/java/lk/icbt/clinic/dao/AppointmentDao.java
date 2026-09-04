package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao {

    Optional<Appointment> findByAppointmentNo(String appointmentNo);

    /**
     * Whether the dentist already has a live booking in this slot. A
     * cancelled appointment does not block the slot.
     */
    boolean existsActiveBooking(Long dentistId, LocalDate date, LocalTime time);

    long countByAppointmentDate(LocalDate date);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByStatusAndAppointmentDate(AppointmentStatus status, LocalDate date);

    /**
     * @throws lk.icbt.clinic.exception.SlotUnavailableException if the unique
     *         constraint on (dentist, date, time) is violated by a concurrent
     *         booking that committed between the caller's check and this save
     */
    Appointment save(Appointment appointment);

    void updateStatus(Appointment appointment);
}
