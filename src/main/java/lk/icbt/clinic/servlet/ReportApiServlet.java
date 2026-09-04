package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.exception.InvalidBookingException;

import java.time.LocalDate;

/** {@code /api/reports/daily-schedule} and {@code /api/reports/daily-revenue}. */
@WebServlet("/api/reports/*")
public class ReportApiServlet extends JsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String which = pathParam(req);
            String dateParam = req.getParameter("date");
            if (dateParam == null) {
                throw new InvalidBookingException("date query parameter is required");
            }
            LocalDate date = LocalDate.parse(dateParam);

            switch (which) {
                case "daily-schedule" -> writeJson(resp, 200, ctx().reportService.dailySchedule(date));
                case "daily-revenue" -> writeJson(resp, 200, ctx().reportService.dailyRevenue(date));
                default -> throw new InvalidBookingException("Unknown report: " + which);
            }
        });
    }
}
