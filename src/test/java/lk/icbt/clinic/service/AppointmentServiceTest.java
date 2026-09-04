package lk.icbt.clinic.service;

import lk.icbt.clinic.dao.AppointmentDao;
import lk.icbt.clinic.dao.DentistDao;
import lk.icbt.clinic.dao.PatientDao;
import lk.icbt.clinic.dao.StaffDao;
import lk.icbt.clinic.dao.TreatmentTypeDao;
import lk.icbt.clinic.exception.AppointmentNotFoundException;
import lk.icbt.clinic.exception.InvalidBookingException;
import lk.icbt.clinic.exception.SlotUnavailableException;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.Dentist;
import lk.icbt.clinic.model.Patient;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.model.StaffRole;
import lk.icbt.clinic.model.TreatmentCode;
import lk.icbt.clinic.model.TreatmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The scenario names double booking, lost records and long waits as the
 * problems the system exists to solve. These tests hold the booking rules
 * that address them, using mocked DAOs so each rule is exercised on its own
 * without a database in the way.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 1);

    @Mock private AppointmentDao appointmentDao;
    @Mock private PatientDao patientDao;
    @Mock private DentistDao dentistDao;
    @Mock private TreatmentTypeDao treatmentTypeDao;
    @Mock private StaffDao staffDao;

    private AppointmentService service;

    private Dentist dentist;
    private TreatmentType treatment;
    private Staff staff;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(COLOMBO).toInstant(), COLOMBO);
        service = new AppointmentService(appointmentDao, patientDao, dentistDao, treatmentTypeDao, staffDao, clock);

        dentist = new Dentist("Dr. Ruwan Silva", "General Dentistry");
        dentist.setId(1L);
        treatment = new TreatmentType(TreatmentCode.FILLING, "Tooth Filling", new BigDecimal("5000.00"));
        treatment.setId(1L);
        staff = new Staff("reception", "hash", "Nimali Perera", StaffRole.RECEPTIONIST);
        staff.setId(1L);
    }

    private BookingRequest request(LocalDate date, LocalTime time) {
        return new BookingRequest("Kamal Jayasuriya", "12 Galle Road, Colombo 03", "0771234567", 1L, 1L, date, time);
    }

    private void happyPathLookups() {
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(patientDao.findByContactNumberAndName(any(), any())).thenReturn(Optional.empty());
        when(patientDao.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("books an appointment and issues it a unique reference")
    void booksAppointmentWithGeneratedReference() {
        happyPathLookups();
        when(appointmentDao.existsActiveBooking(any(), any(), any())).thenReturn(false);
        when(appointmentDao.countByAppointmentDate(any())).thenReturn(0L);
        when(appointmentDao.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment booked = service.book(request(TODAY.plusDays(1), LocalTime.of(10, 0)), 1L);

        assertThat(booked.getAppointmentNo()).isNotBlank();
        assertThat(booked.getDentist()).isSameAs(dentist);
        assertThat(booked.getPatient().getName()).isEqualTo("Kamal Jayasuriya");
    }

    @Test
    @DisplayName("refuses a second booking for the same dentist in the same slot")
    void refusesDoubleBooking() {
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(appointmentDao.existsActiveBooking(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.book(request(TODAY.plusDays(1), LocalTime.of(10, 0)), 1L))
                .isInstanceOf(SlotUnavailableException.class)
                .hasMessageContaining("already has an appointment");

        verify(appointmentDao, never()).save(any());
    }

    @Test
    @DisplayName("refuses to book a date that has already passed")
    void refusesPastDate() {
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> service.book(request(TODAY.minusDays(1), LocalTime.of(10, 0)), 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("past");

        verify(appointmentDao, never()).save(any());
    }

    @Test
    @DisplayName("refuses a time outside clinic opening hours")
    void refusesTimeOutsideOpeningHours() {
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> service.book(request(TODAY.plusDays(1), LocalTime.of(20, 0)), 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("opening hours");
    }

    @Test
    @DisplayName("reuses the existing patient record instead of registering a duplicate")
    void reusesExistingPatient() {
        Patient existing = new Patient("P0001", "Kamal Jayasuriya", "12 Galle Road", "0771234567");
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(patientDao.findByContactNumberAndName("0771234567", "Kamal Jayasuriya"))
                .thenReturn(Optional.of(existing));
        when(appointmentDao.existsActiveBooking(any(), any(), any())).thenReturn(false);
        when(appointmentDao.countByAppointmentDate(any())).thenReturn(3L);
        when(appointmentDao.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment booked = service.book(request(TODAY.plusDays(1), LocalTime.of(11, 0)), 1L);

        assertThat(booked.getPatient()).isSameAs(existing);
        verify(patientDao, never()).save(any());
    }

    @Test
    @DisplayName("rejects a contact number that is not a valid Sri Lankan mobile")
    void rejectsMalformedContactNumber() {
        when(dentistDao.findById(1L)).thenReturn(Optional.of(dentist));
        when(treatmentTypeDao.findById(1L)).thenReturn(Optional.of(treatment));
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));

        BookingRequest bad = new BookingRequest(
                "Kamal Jayasuriya", "12 Galle Road", "12345", 1L, 1L, TODAY.plusDays(1), LocalTime.of(10, 0));

        assertThatThrownBy(() -> service.book(bad, 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("contact number");
    }

    @Test
    @DisplayName("appointment references are unique per day and carry the date")
    void appointmentReferenceIsDatedAndSequential() {
        happyPathLookups();
        when(appointmentDao.existsActiveBooking(any(), any(), any())).thenReturn(false);
        when(appointmentDao.countByAppointmentDate(LocalDate.of(2026, 9, 2))).thenReturn(6L);
        when(appointmentDao.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
        service.book(request(LocalDate.of(2026, 9, 2), LocalTime.of(9, 30)), 1L);
        verify(appointmentDao).save(saved.capture());

        assertThat(saved.getValue().getAppointmentNo()).isEqualTo("APT-20260902-007");
    }

    @Test
    @DisplayName("looking up an unknown appointment number fails clearly")
    void unknownAppointmentNumberFails() {
        when(appointmentDao.findByAppointmentNo("APT-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAppointmentNo("APT-NOPE"))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("APT-NOPE");
    }
}
