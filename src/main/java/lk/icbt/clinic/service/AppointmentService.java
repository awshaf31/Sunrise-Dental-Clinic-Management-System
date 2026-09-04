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
import lk.icbt.clinic.model.TreatmentType;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Booking and retrieving appointments.
 * <p>
 * This is the business tier: servlets do no reasoning of their own, and DAOs
 * do no validation. Dependencies are injected through the constructor by
 * hand, in {@link lk.icbt.clinic.util.AppContext} -- which is what a
 * framework's container would otherwise do -- and that is what lets this
 * whole class be exercised in unit tests against mocked DAOs.
 */
public class AppointmentService {

    private static final DateTimeFormatter REFERENCE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Sri Lankan mobile or landline, with or without the +94 country code. */
    private static final Pattern CONTACT_NUMBER = Pattern.compile("^(?:\\+94|0)\\d{9}$");

    private static final LocalTime OPENING_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(17, 0);

    private final AppointmentDao appointmentDao;
    private final PatientDao patientDao;
    private final DentistDao dentistDao;
    private final TreatmentTypeDao treatmentTypeDao;
    private final StaffDao staffDao;
    private final Clock clock;

    public AppointmentService(AppointmentDao appointmentDao,
                              PatientDao patientDao,
                              DentistDao dentistDao,
                              TreatmentTypeDao treatmentTypeDao,
                              StaffDao staffDao,
                              Clock clock) {
        this.appointmentDao = appointmentDao;
        this.patientDao = patientDao;
        this.dentistDao = dentistDao;
        this.treatmentTypeDao = treatmentTypeDao;
        this.staffDao = staffDao;
        this.clock = clock;
    }

    public Appointment book(BookingRequest request, Long staffId) {
        Dentist dentist = dentistDao.findById(request.dentistId())
                .orElseThrow(() -> new InvalidBookingException("No dentist with id " + request.dentistId()));
        TreatmentType treatment = treatmentTypeDao.findById(request.treatmentTypeId())
                .orElseThrow(() -> new InvalidBookingException("No treatment with id " + request.treatmentTypeId()));
        Staff staff = staffDao.findById(staffId)
                .orElseThrow(() -> new InvalidBookingException("No staff with id " + staffId));

        validate(request);

        if (appointmentDao.existsActiveBooking(dentist.getId(), request.appointmentDate(), request.appointmentTime())) {
            throw new SlotUnavailableException(
                    dentist.getName() + " already has an appointment at "
                            + request.appointmentTime() + " on " + request.appointmentDate());
        }

        Patient patient = findOrRegisterPatient(request);
        String reference = nextAppointmentNo(request.appointmentDate());

        Appointment appointment = new Appointment(
                reference, patient, dentist, treatment,
                request.appointmentDate(), request.appointmentTime(), staff);

        // save() itself catches the uk_dentist_slot constraint violation and
        // rethrows SlotUnavailableException if a concurrent booking won the
        // race between the check above and this insert.
        return appointmentDao.save(appointment);
    }

    public Appointment findByAppointmentNo(String appointmentNo) {
        return appointmentDao.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No appointment found with number " + appointmentNo));
    }

    public List<Appointment> findByDate(LocalDate date) {
        return appointmentDao.findByAppointmentDate(date);
    }

    public Appointment cancel(String appointmentNo) {
        Appointment appointment = findByAppointmentNo(appointmentNo);
        appointment.cancel();
        appointmentDao.updateStatus(appointment);
        return appointment;
    }

    private void validate(BookingRequest request) {
        if (request.patientName() == null || request.patientName().isBlank()) {
            throw new InvalidBookingException("Patient name is required");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new InvalidBookingException("Address is required");
        }
        if (request.contactNumber() == null
                || !CONTACT_NUMBER.matcher(request.contactNumber().trim()).matches()) {
            throw new InvalidBookingException(
                    "The contact number must be a valid Sri Lankan number, for example 0771234567");
        }
        if (request.appointmentDate() == null || request.appointmentTime() == null) {
            throw new InvalidBookingException("Appointment date and time are required");
        }
        if (request.appointmentDate().isBefore(LocalDate.now(clock))) {
            throw new InvalidBookingException("An appointment cannot be booked in the past");
        }
        if (request.appointmentTime().isBefore(OPENING_TIME) || request.appointmentTime().isAfter(CLOSING_TIME)) {
            throw new InvalidBookingException(
                    "The clinic's opening hours are " + OPENING_TIME + " to " + CLOSING_TIME);
        }
    }

    /**
     * Returning patients keep one record, so their history stays in one place
     * instead of accumulating the duplicate files the scenario complains about.
     */
    private Patient findOrRegisterPatient(BookingRequest request) {
        return patientDao
                .findByContactNumberAndName(request.contactNumber().trim(), request.patientName().trim())
                .orElseGet(() -> patientDao.save(new Patient(
                        nextPatientNo(),
                        request.patientName().trim(),
                        request.address().trim(),
                        request.contactNumber().trim())));
    }

    /** Human-readable and sortable: APT-20260902-007 is the 7th booking that day. */
    private String nextAppointmentNo(LocalDate date) {
        long sequence = appointmentDao.countByAppointmentDate(date) + 1;
        return "APT-%s-%03d".formatted(date.format(REFERENCE_DATE), sequence);
    }

    private String nextPatientNo() {
        return "PAT-%d".formatted(patientDao.count() + 1);
    }
}
