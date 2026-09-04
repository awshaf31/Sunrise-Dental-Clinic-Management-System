package lk.icbt.clinic.dto;

import java.math.BigDecimal;

/** One line of the daily revenue report, as returned by sp_daily_revenue. */
public record DailyRevenueRow(
        String treatmentName, long billsIssued,
        BigDecimal consultationTotal, BigDecimal treatmentTotal, BigDecimal revenueTotal) {
}
