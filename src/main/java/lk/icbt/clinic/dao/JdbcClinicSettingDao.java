package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.ClinicSetting;
import lk.icbt.clinic.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcClinicSettingDao implements ClinicSettingDao {

    @Override
    public Optional<ClinicSetting> findByKey(String key) {
        String sql = "SELECT setting_key, setting_value, description FROM clinic_setting WHERE setting_key = ?";
        try (Connection c = DbConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new ClinicSetting(
                        rs.getString("setting_key"), rs.getString("setting_value"), rs.getString("description")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read clinic setting: " + key, e);
        }
    }
}
