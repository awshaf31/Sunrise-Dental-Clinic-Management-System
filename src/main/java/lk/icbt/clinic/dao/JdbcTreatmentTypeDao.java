package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.TreatmentCode;
import lk.icbt.clinic.model.TreatmentType;
import lk.icbt.clinic.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTreatmentTypeDao implements TreatmentTypeDao {

    @Override
    public List<TreatmentType> findAllActive() {
        String sql = "SELECT id, code, name, base_fee, active FROM treatment_type WHERE active = TRUE ORDER BY name";
        List<TreatmentType> out = new ArrayList<>();
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list treatment types", e);
        }
    }

    @Override
    public Optional<TreatmentType> findById(Long id) {
        String sql = "SELECT id, code, name, base_fee, active FROM treatment_type WHERE id = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up treatment type: " + id, e);
        }
    }

    private TreatmentType map(ResultSet rs) throws SQLException {
        TreatmentType t = new TreatmentType();
        t.setId(rs.getLong("id"));
        t.setCode(TreatmentCode.valueOf(rs.getString("code")));
        t.setName(rs.getString("name"));
        t.setBaseFee(rs.getBigDecimal("base_fee"));
        t.setActive(rs.getBoolean("active"));
        return t;
    }
}
