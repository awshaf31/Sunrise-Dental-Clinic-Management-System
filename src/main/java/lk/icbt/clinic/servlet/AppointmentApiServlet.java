package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.AppointmentResponse;
import lk.icbt.clinic.dto.CreateAppointmentRequest;
import lk.icbt.clinic.exception.InvalidBookingException;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.service.BookingRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * The appointment web service: {@code /api/appointments} and
 * {@code /api/appointments/{no}[/cancel]}.
 * <p>
 * One servlet handles all three shapes by branching on {@code pathInfo},
 * because the Jakarta Servlet API -- unlike Spring MVC's
 * {@code @GetMapping("/{no}")} -- has no built-in path-variable extraction.
 * Writing that extraction by hand is exactly what {@link JsonServlet#pathParam}
 * exists for.
 */
@WebServlet("/api/appointments/*")
public class AppointmentApiServlet extends JsonServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.endsWith("/cancel")) {
                String appointmentNo = pathInfo.substring(1, pathInfo.length() - "/cancel".length());
                Appointment cancelled = ctx().appointmentService.cancel(appointmentNo);
                writeJson(resp, 200, new AppointmentResponse(cancelled));
                return;
            }

            Long staffId = staffIdHeader(req);
            CreateAppointmentRequest body = readBody(req, CreateAppointmentRequest.class);
            BookingRequest booking = new BookingRequest(
                    body.patientName, body.address, body.contactNumber,
                    body.dentistId, body.treatmentTypeId, body.appointmentDate, body.appointmentTime);

            Appointment booked = ctx().appointmentService.book(booking, staffId);
            writeJson(resp, 201, new AppointmentResponse(booked));
        });
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/appointments?date=2026-09-03
                String dateParam = req.getParameter("date");
                if (dateParam == null) {
                    throw new InvalidBookingException("date query parameter is required");
                }
                List<AppointmentResponse> list = ctx().appointmentService.findByDate(LocalDate.parse(dateParam))
                        .stream().map(AppointmentResponse::new).toList();
                writeJson(resp, 200, list);
                return;
            }

            // GET /api/appointments/{no}
            String appointmentNo = pathParam(req);
            Appointment appointment = ctx().appointmentService.findByAppointmentNo(appointmentNo);
            writeJson(resp, 200, new AppointmentResponse(appointment));
        });
    }
}
