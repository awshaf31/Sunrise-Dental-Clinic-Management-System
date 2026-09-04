package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Patient;
import lk.icbt.clinic.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcPatientDao implements PatientDao {

    @Override
    public Optional<Patient> findByContactNumberAndName(String contactNumber, String name) {
        String sql = """
                SELECT id, patient_no, name, address, contact_number, created_at
                FROM patient WHERE contact_number = ? AND LOWER(name) = LOWER(?)
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up patient", e);
        }
    }

    @Override
    public Patient save(Patient patient) {
        String sql = """
                INSERT INTO patient (patient_no, name, address, contact_number)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getPatientNo());
            ps.setString(2, patient.getName());
            ps.setString(3, patient.getAddress());
            ps.setString(4, patient.getContactNumber());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) patient.setId(keys.getLong(1));
            }
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM patient";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count patients", e);
        }
    }

    private Patient map(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getLong("id"));
        p.setPatientNo(rs.getString("patient_no"));
        p.setName(rs.getString("name"));
        p.setAddress(rs.getString("address"));
        p.setContactNumber(rs.getString("contact_number"));
        var ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}
