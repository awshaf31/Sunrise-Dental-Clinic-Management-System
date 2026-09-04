package lk.icbt.clinic.service;

import lk.icbt.clinic.dao.AppointmentDao;
import lk.icbt.clinic.dao.ReportDao;
import lk.icbt.clinic.dto.DailyRevenueRow;
import lk.icbt.clinic.dto.DailyScheduleRow;
import lk.icbt.clinic.exception.ReportUnavailableException;
import lk.icbt.clinic.model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * The management reports the clinic runs its day on.
 * <p>
 * The revenue breakdown is produced by the {@code sp_daily_revenue} stored
 * procedure; the schedule is a plain query walked in Java. Choosing a stored
 * procedure for one and not the other is deliberate: aggregation belongs
 * where the data lives, but a straight ordered selection gives the database
 * nothing to do that Java could not do equally cheaply.
 */
public class ReportService {

    private final ReportDao reportDao;
    private final AppointmentDao appointmentDao;

    public ReportService(ReportDao reportDao, AppointmentDao appointmentDao) {
        this.reportDao = reportDao;
        this.appointmentDao = appointmentDao;
    }

    public List<DailyRevenueRow> dailyRevenue(LocalDate date) {
        try {
            return reportDao.dailyRevenue(date);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException sqlEx
                    && (sqlEx.getErrorCode() == 1305 || "42000".equals(sqlEx.getSQLState()))) {
                // MySQL error 1305: PROCEDURE ... does not exist -- the
                // routine genuinely was not applied to this database.
                throw new ReportUnavailableException(
                        "The revenue report needs the sp_daily_revenue stored procedure, "
                                + "which has not been applied to this database.", e);
            }
            throw e;
        }
    }

    public List<DailyScheduleRow> dailySchedule(LocalDate date) {
        List<Appointment> appointments = appointmentDao.findByAppointmentDate(date);
        return appointments.stream()
                .map(a -> new DailyScheduleRow(
                        a.getAppointmentTime(), a.getAppointmentNo(), a.getPatient().getName(),
                        a.getPatient().getContactNumber(), a.getDentist().getName(),
                        a.getTreatmentType().getName(), a.getStatus().name()))
                .toList();
    }
}
