package lk.icbt.clinic.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dao.DentistDao;
import lk.icbt.clinic.dao.TreatmentTypeDao;
import lk.icbt.clinic.exception.SlotUnavailableException;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.Dentist;
import lk.icbt.clinic.model.Patient;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.model.StaffRole;
import lk.icbt.clinic.model.TreatmentCode;
import lk.icbt.clinic.model.TreatmentType;
import lk.icbt.clinic.service.AppointmentService;
import lk.icbt.clinic.service.AuthService;
import lk.icbt.clinic.service.BillingService;
import lk.icbt.clinic.service.ReportService;
import lk.icbt.clinic.util.AppContext;
import lk.icbt.clinic.util.AppInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Drives {@link AppointmentApiServlet} directly, the way {@code MockMvc}
 * drives a Spring controller -- except there is no embedded servlet
 * container here, only Mockito doubles for the request and response. This
 * proves the web service's HTTP contract (status codes, JSON body) without
 * needing Tomcat running, and without the unit tests' mocks moving up a
 * layer to hide a wiring mistake between the servlet and the service.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentApiServletTest {

    @Mock private AppointmentService appointmentService;
    @Mock private HttpServletRequest req;
    @Mock private HttpServletResponse resp;
    @Mock private ServletContext servletContext;

    private AppointmentApiServlet servlet;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        AppContext ctx = new AppContext(
                (AuthService) null, appointmentService, (BillingService) null, (ReportService) null,
                (DentistDao) null, (TreatmentTypeDao) null);

        lenient().when(servletContext.getAttribute(AppInitializer.CONTEXT_KEY)).thenReturn(ctx);
        lenient().when(req.getServletContext()).thenReturn(servletContext);

        responseBody = new StringWriter();
        lenient().when(resp.getWriter()).thenReturn(new PrintWriter(responseBody));

        servlet = new AppointmentApiServlet() {
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
        };
    }

    private Appointment sampleAppointment() {
        Dentist d = new Dentist("Dr. Ruwan Silva", "General Dentistry");
        d.setId(1L);
        TreatmentType t = new TreatmentType(TreatmentCode.FILLING, "Tooth Filling", new BigDecimal("5000.00"));
        t.setId(1L);
        Patient p = new Patient("PAT-1", "Kamal Jayasuriya", "12 Galle Road", "0771234567");
        Staff s = new Staff("reception", "hash", "Nimali Perera", StaffRole.RECEPTIONIST);
        s.setId(1L);
        return new Appointment("APT-20260902-007", p, d, t,
                LocalDate.of(2026, 9, 2), LocalTime.of(9, 0), s);
    }

    @Test
    @DisplayName("POST /api/appointments books a visit and returns 201 with its reference")
    void booksAnAppointment() throws Exception {
        when(req.getHeader("X-Staff-Id")).thenReturn("1");
        String body = """
                {"patientName":"Kamal Jayasuriya","address":"12 Galle Road","contactNumber":"0771234567",
                 "dentistId":1,"treatmentTypeId":1,"appointmentDate":"2026-09-02","appointmentTime":"09:00"}""";
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
        when(appointmentService.book(any(), anyLong())).thenReturn(sampleAppointment());

        servlet.doPost(req, resp);

        verifyStatus(201);
        assertThat(responseBody.toString())
                .contains("\"appointmentNo\":\"APT-20260902-007\"")
                .contains("\"patientName\":\"Kamal Jayasuriya\"");
    }

    @Test
    @DisplayName("booking the same dentist twice in one slot returns 409 Conflict")
    void rejectsDoubleBookingWithConflict() throws Exception {
        when(req.getHeader("X-Staff-Id")).thenReturn("1");
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(
                """
                {"patientName":"Kamal","address":"12 Galle Road","contactNumber":"0771234567",
                 "dentistId":1,"treatmentTypeId":1,"appointmentDate":"2026-09-02","appointmentTime":"09:00"}""")));
        when(appointmentService.book(any(), anyLong()))
                .thenThrow(new SlotUnavailableException("Dr. Ruwan Silva already has an appointment at 09:00 on 2026-09-02"));

        servlet.doPost(req, resp);

        verifyStatus(409);
        assertThat(responseBody.toString())
                .contains("\"error\":\"Slot Unavailable\"")
                .contains("already has an appointment");
    }

    @Test
    @DisplayName("GET /api/appointments/{no} returns the booking")
    void findsAnAppointmentByReference() throws Exception {
        when(req.getPathInfo()).thenReturn("/APT-20260902-007");
        when(appointmentService.findByAppointmentNo("APT-20260902-007")).thenReturn(sampleAppointment());

        servlet.doGet(req, resp);

        verifyStatus(200);
        assertThat(responseBody.toString()).contains("\"treatmentName\":\"Tooth Filling\"");
    }

    private void verifyStatus(int expected) {
        org.mockito.Mockito.verify(resp).setStatus(expected);
    }
}
