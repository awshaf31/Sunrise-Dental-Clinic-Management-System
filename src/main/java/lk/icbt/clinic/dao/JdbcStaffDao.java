package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.model.StaffRole;
import lk.icbt.clinic.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Plain JDBC implementation of {@link StaffDao}. Every column is read and
 * written by hand -- there is no annotation or configuration file mapping
 * {@link Staff} to the {@code staff} table; this class <em>is</em> the mapping.
 */
public class JdbcStaffDao implements StaffDao {

    @Override
    public Optional<Staff> findByUsernameAndActive(String username) {
        String sql = """
                SELECT id, username, password_hash, full_name, role, active, created_at
                FROM staff WHERE username = ? AND active = TRUE
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up staff by username: " + username, e);
        }
    }

    @Override
    public Optional<Staff> findById(Long id) {
        String sql = """
                SELECT id, username, password_hash, full_name, role, active, created_at
                FROM staff WHERE id = ?
                """;
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up staff by id: " + id, e);
        }
    }

    private Staff map(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setId(rs.getLong("id"));
        s.setUsername(rs.getString("username"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setFullName(rs.getString("full_name"));
        s.setRole(StaffRole.valueOf(rs.getString("role")));
        s.setActive(rs.getBoolean("active"));
        var ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}
