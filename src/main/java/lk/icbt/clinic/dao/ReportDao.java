package lk.icbt.clinic.dao;

import lk.icbt.clinic.dto.DailyRevenueRow;

import java.time.LocalDate;
import java.util.List;

public interface ReportDao {

    /** Calls the {@code sp_daily_revenue} stored procedure. MySQL-only. */
    List<DailyRevenueRow> dailyRevenue(LocalDate date);
}
