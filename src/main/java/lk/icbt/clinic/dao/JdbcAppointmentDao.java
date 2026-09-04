package lk.icbt.clinic.dao;

import lk.icbt.clinic.exception.SlotUnavailableException;
import lk.icbt.clinic.model.*;
import lk.icbt.clinic.util.DbConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Plain JDBC implementation of {@link AppointmentDao}.
 * <p>
 * Reading an appointment means joining four tables by hand, because there is
 * no ORM to walk object references for us -- the SELECT lists every column
 * needed to reconstruct the patient, dentist, treatment type and staff
 * member in one round trip.
 */
public class JdbcAppointmentDao implements AppointmentDao {

    /** MySQL's error code for a UNIQUE constraint violation (ER_DUP_ENTRY). */
    private static final int DUPLICATE_ENTRY = 1062;

    private static final String SELECT_JOINED = """
            SELECT a.id, a.appointment_no, a.appointment_date, a.appointment_time,
                   a.status, a.created_at,
                   p.id AS p_id, p.patient_no, p.name AS p_name, p.address, p.contact_number, p.created_at AS p_created_at,
                   d.id AS d_id, d.name AS d_name, d.specialization, d.active AS d_active,
                   t.id AS t_id, t.code, t.name AS t_name, t.base_fee, t.active AS t_active,
                   s.id AS s_id, s.username, s.full_name, s.role, s.active AS s_active
            FROM appointment a
            JOIN patient p        ON p.id = a.patient_id
            JOIN dentist d        ON d.id = a.dentist_id
            JOIN treatment_type t ON t.id = a.treatment_type_id
            JOIN staff s          ON s.id = a.created_by
            """;

    @Override
    public Optional<Appointment> findByAppointmentNo(String appointmentNo) {
        String sql = SELECT_JOINED + " WHERE a.appointment_no = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up appointment: " + appointmentNo, e);
        }
    }

    @Override
    public boolean existsActiveBooking(Long dentistId, LocalDate date, LocalTime time) {
        String sql = """
                SELECT COUNT(*) FROM appointment
                WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ?
                  AND status <> 'CANCELLED'
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, dentistId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check dentist availability", e);
        }
    }

    @Override
    public long countByAppointmentDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointment WHERE appointment_date = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count appointments for " + date, e);
        }
    }

    @Override
    public List<Appointment> findByAppointmentDate(LocalDate date) {
        String sql = SELECT_JOINED + " WHERE a.appointment_date = ? ORDER BY a.appointment_time";
        return queryList(sql, ps -> ps.setDate(1, Date.valueOf(date)));
    }

    @Override
    public List<Appointment> findByStatusAndAppointmentDate(AppointmentStatus status, LocalDate date) {
        String sql = SELECT_JOINED + " WHERE a.status = ? AND a.appointment_date = ? ORDER BY a.appointment_time";
        return queryList(sql, ps -> {
            ps.setString(1, status.name());
            ps.setDate(2, Date.valueOf(date));
        });
    }

    @Override
    public Appointment save(Appointment appointment) {
        String sql = """
                INSERT INTO appointment
                    (appointment_no, patient_id, dentist_id, treatment_type_id,
                     appointment_date, appointment_time, status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, appointment.getAppointmentNo());
            ps.setLong(2, appointment.getPatient().getId());
            ps.setLong(3, appointment.getDentist().getId());
            ps.setLong(4, appointment.getTreatmentType().getId());
            ps.setDate(5, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(6, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(7, appointment.getStatus().name());
            ps.setLong(8, appointment.getCreatedBy().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) appointment.setId(keys.getLong(1));
            }
            return appointment;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw translateConstraintViolation(appointment, e);
        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                throw translateConstraintViolation(appointment, e);
            }
            throw new RuntimeException("Failed to save appointment", e);
        }
    }

    /**
     * The service layer already checked {@code existsActiveBooking} before
     * calling this method, but that check can be overtaken by a concurrent
     * booking that commits in between -- the {@code uk_dentist_slot} UNIQUE
     * constraint is the actual guarantee. This turns its violation back into
     * the same exception the caller already handles, so correctness never
     * depends on the timing of two requests.
     */
    private SlotUnavailableException translateConstraintViolation(Appointment a, SQLException e) {
        return new SlotUnavailableException(
                a.getDentist().getName() + " was booked for " + a.getAppointmentTime()
                        + " on " + a.getAppointmentDate() + " by another user moments ago");
    }

    @Override
    public void updateStatus(Appointment appointment) {
        String sql = "UPDATE appointment SET status = ? WHERE id = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, appointment.getStatus().name());
            ps.setLong(2, appointment.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment status", e);
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Appointment> queryList(String sql, Binder binder) {
        List<Appointment> out = new ArrayList<>();
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query appointments", e);
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getLong("p_id"));
        patient.setPatientNo(rs.getString("patient_no"));
        patient.setName(rs.getString("p_name"));
        patient.setAddress(rs.getString("address"));
        patient.setContactNumber(rs.getString("contact_number"));

        Dentist dentist = new Dentist();
        dentist.setId(rs.getLong("d_id"));
        dentist.setName(rs.getString("d_name"));
        dentist.setSpecialization(rs.getString("specialization"));
        dentist.setActive(rs.getBoolean("d_active"));

        TreatmentType treatment = new TreatmentType();
        treatment.setId(rs.getLong("t_id"));
        treatment.setCode(TreatmentCode.valueOf(rs.getString("code")));
        treatment.setName(rs.getString("t_name"));
        treatment.setBaseFee(rs.getBigDecimal("base_fee"));
        treatment.setActive(rs.getBoolean("t_active"));

        Staff staff = new Staff();
        staff.setId(rs.getLong("s_id"));
        staff.setUsername(rs.getString("username"));
        staff.setFullName(rs.getString("full_name"));
        staff.setRole(StaffRole.valueOf(rs.getString("role")));
        staff.setActive(rs.getBoolean("s_active"));

        Appointment a = new Appointment();
        a.setId(rs.getLong("id"));
        a.setAppointmentNo(rs.getString("appointment_no"));
        a.setPatient(patient);
        a.setDentist(dentist);
        a.setTreatmentType(treatment);
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setCreatedBy(staff);
        var ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}
