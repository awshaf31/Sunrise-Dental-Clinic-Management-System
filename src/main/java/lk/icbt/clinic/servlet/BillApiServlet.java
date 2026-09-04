package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.BillResponse;
import lk.icbt.clinic.model.Bill;

/** {@code /api/bills/{appointmentNo}} -- issue or retrieve a bill. */
@WebServlet("/api/bills/*")
public class BillApiServlet extends JsonServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String appointmentNo = pathParam(req);
            Long staffId = staffIdHeader(req);
            Bill bill = ctx().billingService.issueBill(appointmentNo, staffId);
            writeJson(resp, 200, new BillResponse(bill));
        });
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String appointmentNo = pathParam(req);
            Bill bill = ctx().billingService.findByAppointmentNo(appointmentNo);
            writeJson(resp, 200, new BillResponse(bill));
        });
    }
}
