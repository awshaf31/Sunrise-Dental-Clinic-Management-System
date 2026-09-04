package lk.icbt.clinic.servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.DailyScheduleRow;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.Dentist;
import lk.icbt.clinic.service.BookingRequest;
import lk.icbt.clinic.util.GsonProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The booking, search, view and cancel pages under {@code /appointments/*}.
 * <p>
 * Handles several distinct URLs in one servlet by branching on
 * {@code getServletPath()} / {@code getPathInfo()} -- the same trade-off
 * Spring's {@code @RequestMapping} hides behind annotations, made explicit.
 */
@WebServlet({"/appointments/new", "/appointments", "/appointments/search",
             "/appointments/*", "/appointments/booked-times"})
public class AppointmentWebServlet extends ViewServlet {

    /**
     * The clinic's bookable hours. Set as a request attribute rather than
     * written inline in the JSP: EL 3.0 supports list literals
     * ({@code ${['09:00', ...]}}), but relying on that is a needless
     * dependency on the exact EL implementation version Tomcat bundles.
     * Plain data belongs in Java, not embedded in a view.
     */
    private static final List<String> OPENING_HOURS =
            List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00");

    private final Gson gson = GsonProvider.get();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if (path.equals("/appointments/new")) {
            showBookingForm(req, resp, null);
            return;
        }
        if (path.equals("/appointments/search")) {
            req.setAttribute("activeNav", "search");
            render(req, resp, "appointment/search.jsp");
            return;
        }
        if (path.equals("/appointments/booked-times")) {
            respondBookedTimes(req, resp);
            return;
        }
        if (path.equals("/appointments") && pathInfo != null && !pathInfo.equals("/")) {
            String appointmentNo = pathInfo.substring(1);
            handle(req, resp, () -> {
                Appointment a = ctx().appointmentService.findByAppointmentNo(appointmentNo);
                req.setAttribute("appointment", a);
                render(req, resp, "appointment/view.jsp");
            });
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if (path.equals("/appointments/search")) {
            String appointmentNo = req.getParameter("appointmentNo");
            handle(req, resp, () -> {
                try {
                    Appointment a = ctx().appointmentService
                            .findByAppointmentNo(appointmentNo == null ? "" : appointmentNo.trim());
                    req.setAttribute("appointment", a);
                    render(req, resp, "appointment/view.jsp");
                } catch (RuntimeException e) {
                    req.setAttribute("error", e.getMessage());
                    req.setAttribute("appointmentNo", appointmentNo);
                    req.setAttribute("activeNav", "search");
                    render(req, resp, "appointment/search.jsp");
                }
            });
            return;
        }

        if (path.equals("/appointments") && pathInfo != null && pathInfo.endsWith("/cancel")) {
            String appointmentNo = pathInfo.substring(1, pathInfo.length() - "/cancel".length());
            ctx().appointmentService.cancel(appointmentNo);
            resp.sendRedirect(req.getContextPath() + "/appointments/" + appointmentNo);
            return;
        }

        if (path.equals("/appointments")) {
            bookAppointment(req, resp);
        }
    }

    private void bookAppointment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String patientName = req.getParameter("patientName");
        String address = req.getParameter("address");
        String contactNumber = req.getParameter("contactNumber");
        Long dentistId = parseLong(req.getParameter("dentistId"));
        Long treatmentTypeId = parseLong(req.getParameter("treatmentTypeId"));
        LocalDate date = parseDate(req.getParameter("appointmentDate"));
        LocalTime time = parseTime(req.getParameter("appointmentTime"));

        try {
            var staff = currentUser(req);
            BookingRequest booking = new BookingRequest(
                    patientName, address, contactNumber, dentistId, treatmentTypeId, date, time);
            Appointment booked = ctx().appointmentService.book(booking, staff.getId());

            req.getSession().setAttribute("flash.success",
                    "Appointment " + booked.getAppointmentNo() + " booked for " + booked.getPatient().getName() + ".");
            resp.sendRedirect(req.getContextPath() + "/appointments/" + booked.getAppointmentNo());

        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("fieldErrors", FieldErrorMapper.mapFrom(e.getMessage()));
            req.setAttribute("patientName", patientName);
            req.setAttribute("address", address);
            req.setAttribute("contactNumber", contactNumber);
            req.setAttribute("dentistId", dentistId);
            req.setAttribute("treatmentTypeId", treatmentTypeId);
            req.setAttribute("appointmentDate", date);
            req.setAttribute("appointmentTime", time);
            showBookingForm(req, resp, e.getMessage());
        }
    }

    private void showBookingForm(HttpServletRequest req, HttpServletResponse resp, String ignoredError) {
        req.setAttribute("activeNav", "new");
        req.setAttribute("dentists", ctx().dentistDao.findAllActive());
        req.setAttribute("treatments", ctx().treatmentTypeDao.findAllActive());
        req.setAttribute("hours", OPENING_HOURS);
        if (req.getAttribute("appointmentDate") == null) {
            req.setAttribute("appointmentDate", LocalDate.now());
        }
        render(req, resp, "appointment/new.jsp");
    }

    /** Backs the day-rail's live availability check on the booking form. */
    private void respondBookedTimes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long dentistId = parseLong(req.getParameter("dentistId"));
        LocalDate date = parseDate(req.getParameter("date"));
        if (dentistId == null || date == null) {
            resp.setStatus(400);
            return;
        }

        List<DailyScheduleRow> schedule = ctx().reportService.dailySchedule(date);
        String dentistName = ctx().dentistDao.findById(dentistId).map(Dentist::getName).orElse(null);

        List<String> bookedTimes = schedule.stream()
                .filter(row -> !"CANCELLED".equals(row.status()))
                .filter(row -> row.dentistName().equals(dentistName))
                .map(row -> row.appointmentTime().toString())
                .toList();

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(bookedTimes));
    }

    private Long parseLong(String s) {
        try {
            return s == null || s.isBlank() ? null : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String s) {
        try {
            return s == null || s.isBlank() ? null : LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String s) {
        try {
            return s == null || s.isBlank() ? null : LocalTime.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
