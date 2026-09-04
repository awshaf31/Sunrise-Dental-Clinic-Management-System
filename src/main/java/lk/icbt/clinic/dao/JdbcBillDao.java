package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.*;
import lk.icbt.clinic.util.DbConnection;

import java.sql.*;
import java.util.Optional;

/**
 * Plain JDBC implementation of {@link BillDao}.
 * <p>
 * A bill is meaningless without the appointment it settles (the receipt
 * needs the patient, dentist and treatment names), so {@link #findByAppointmentNo}
 * joins across all five tables in one query rather than returning a bill with
 * a null appointment and forcing the caller to make a second round trip.
 */
public class JdbcBillDao implements BillDao {

    private static final String SELECT_JOINED = """
            SELECT b.id, b.consultation_fee, b.treatment_fee, b.total_amount, b.issued_at,
                   a.id AS a_id, a.appointment_no, a.appointment_date, a.appointment_time,
                   a.status, a.created_at AS a_created_at,
                   p.id AS p_id, p.patient_no, p.name AS p_name, p.address, p.contact_number,
                   d.id AS d_id, d.name AS d_name, d.specialization, d.active AS d_active,
                   t.id AS t_id, t.code, t.name AS t_name, t.base_fee, t.active AS t_active,
                   ib.id AS ib_id, ib.username AS ib_username, ib.full_name AS ib_full_name,
                   ib.role AS ib_role, ib.active AS ib_active
            FROM bill b
            JOIN appointment a     ON a.id = b.appointment_id
            JOIN patient p         ON p.id = a.patient_id
            JOIN dentist d         ON d.id = a.dentist_id
            JOIN treatment_type t  ON t.id = a.treatment_type_id
            JOIN staff ib          ON ib.id = b.issued_by
            """;

    @Override
    public Optional<Bill> findByAppointmentNo(String appointmentNo) {
        String sql = SELECT_JOINED + " WHERE a.appointment_no = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up bill for " + appointmentNo, e);
        }
    }

    @Override
    public Bill save(Bill bill) {
        String sql = """
                INSERT INTO bill (appointment_id, consultation_fee, treatment_fee, total_amount, issued_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, bill.getAppointment().getId());
            ps.setBigDecimal(2, bill.getConsultationFee());
            ps.setBigDecimal(3, bill.getTreatmentFee());
            ps.setBigDecimal(4, bill.getTotalAmount());
            ps.setLong(5, bill.getIssuedBy().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) bill.setId(keys.getLong(1));
            }
            return bill;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                // uk_bill_appointment: a bill was raised for this appointment
                // by a concurrent request between the caller's check and this
                // insert. The service layer catches this and re-reads the
                // winning bill rather than treating it as a hard failure.
                throw new IllegalStateException(
                        "A bill was already issued for this appointment by another request", e);
            }
            throw new RuntimeException("Failed to save bill", e);
        }
    }

    private Bill map(ResultSet rs) throws SQLException {
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

        Appointment appointment = new Appointment();
        appointment.setId(rs.getLong("a_id"));
        appointment.setAppointmentNo(rs.getString("appointment_no"));
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatmentType(treatment);
        appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        appointment.setStatus(AppointmentStatus.valueOf(rs.getString("status")));

        Staff issuedBy = new Staff();
        issuedBy.setId(rs.getLong("ib_id"));
        issuedBy.setUsername(rs.getString("ib_username"));
        issuedBy.setFullName(rs.getString("ib_full_name"));
        issuedBy.setRole(StaffRole.valueOf(rs.getString("ib_role")));
        issuedBy.setActive(rs.getBoolean("ib_active"));

        Bill bill = new Bill();
        bill.setId(rs.getLong("id"));
        bill.setAppointment(appointment);
        bill.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        bill.setTreatmentFee(rs.getBigDecimal("treatment_fee"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setIssuedBy(issuedBy);
        var ts = rs.getTimestamp("issued_at");
        if (ts != null) bill.setIssuedAt(ts.toLocalDateTime());
        return bill;
    }
}
