package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Dentist;
import lk.icbt.clinic.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDentistDao implements DentistDao {

    @Override
    public List<Dentist> findAllActive() {
        String sql = "SELECT id, name, specialization, active FROM dentist WHERE active = TRUE ORDER BY name";
        List<Dentist> out = new ArrayList<>();
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list dentists", e);
        }
    }

    @Override
    public Optional<Dentist> findById(Long id) {
        String sql = "SELECT id, name, specialization, active FROM dentist WHERE id = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up dentist: " + id, e);
        }
    }

    private Dentist map(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setActive(rs.getBoolean("active"));
        return d;
    }
}
