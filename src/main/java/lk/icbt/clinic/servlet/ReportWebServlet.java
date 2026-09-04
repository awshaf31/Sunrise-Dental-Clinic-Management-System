package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.DailyRevenueRow;
import lk.icbt.clinic.exception.ReportUnavailableException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@WebServlet("/reports")
public class ReportWebServlet extends ViewServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(req, resp, () -> {
            String dateParam = req.getParameter("date");
            LocalDate date = dateParam == null ? LocalDate.now() : LocalDate.parse(dateParam);

            req.setAttribute("activeNav", "reports");
            req.setAttribute("date", date);
            req.setAttribute("displayDate", date.format(
                    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH)));
            req.setAttribute("schedule", ctx().reportService.dailySchedule(date));

            try {
                List<DailyRevenueRow> revenue = ctx().reportService.dailyRevenue(date);
                req.setAttribute("revenue", revenue);
                req.setAttribute("revenueTotal", revenue.stream()
                        .map(DailyRevenueRow::revenueTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
            } catch (ReportUnavailableException e) {
                req.setAttribute("revenueError", e.getMessage());
            }
            render(req, resp, "reports/index.jsp");
        });
    }
}
