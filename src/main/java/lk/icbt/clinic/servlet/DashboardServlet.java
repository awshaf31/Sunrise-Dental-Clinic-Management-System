package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.DailyScheduleRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@WebServlet("")
public class DashboardServlet extends ViewServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(req, resp, () -> {
            String dateParam = req.getParameter("date");
            LocalDate date = dateParam == null ? LocalDate.now() : LocalDate.parse(dateParam);

            List<DailyScheduleRow> schedule = ctx().reportService.dailySchedule(date);
            long booked = schedule.stream().filter(r -> !"CANCELLED".equals(r.status())).count();
            long cancelled = schedule.stream().filter(r -> "CANCELLED".equals(r.status())).count();

            req.setAttribute("activeNav", "today");
            req.setAttribute("date", date);
            req.setAttribute("displayDate", date.format(
                    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH)));
            req.setAttribute("schedule", schedule);
            req.setAttribute("slots", DayRailBuilder.build(schedule));
            req.setAttribute("bookedCount", booked);
            req.setAttribute("cancelledCount", cancelled);
            render(req, resp, "dashboard.jsp");
        });
    }
}
