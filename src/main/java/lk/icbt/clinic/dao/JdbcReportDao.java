package lk.icbt.clinic.dao;

import lk.icbt.clinic.dto.DailyRevenueRow;
import lk.icbt.clinic.util.DbConnection;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls {@code sp_daily_revenue} through a plain JDBC {@link CallableStatement}.
 * <p>
 * There is no ORM here to hide the call behind a repository method that
 * "just returns entities" -- invoking a stored procedure from raw JDBC means
 * writing the {@code {call ...}} escape syntax and walking the
 * {@link ResultSet} by hand, exactly as shown here. This is also the reason
 * aggregation lives in the database rather than in Java: one small result
 * set crosses the network instead of every bill raised that day.
 */
public class JdbcReportDao implements ReportDao {

    @Override
    public List<DailyRevenueRow> dailyRevenue(LocalDate date) {
        String call = "{call sp_daily_revenue(?)}";
        List<DailyRevenueRow> rows = new ArrayList<>();
        try (Connection c = DbConnection.get();
             CallableStatement cs = c.prepareCall(call)) {
            cs.setDate(1, Date.valueOf(date));
            boolean hasResultSet = cs.execute();
            if (hasResultSet) {
                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) {
                        rows.add(new DailyRevenueRow(
                                rs.getString("treatment_name"),
                                rs.getLong("bills_issued"),
                                nz(rs.getBigDecimal("consultation_total")),
                                nz(rs.getBigDecimal("treatment_total")),
                                nz(rs.getBigDecimal("revenue_total"))));
                    }
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to call sp_daily_revenue for " + date, e);
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
