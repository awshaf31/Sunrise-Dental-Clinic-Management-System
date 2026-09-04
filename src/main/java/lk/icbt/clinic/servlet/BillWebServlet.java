package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bills/*")
public class BillWebServlet extends ViewServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        issue(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        issue(req, resp);
    }

    /** Safe to call twice: the service returns the existing bill rather than issuing a second. */
    private void issue(HttpServletRequest req, HttpServletResponse resp) {
        handle(req, resp, () -> {
            String appointmentNo = req.getPathInfo().substring(1);
            var bill = ctx().billingService.issueBill(appointmentNo, currentUser(req).getId());
            req.setAttribute("bill", bill);
            render(req, resp, "bill/receipt.jsp");
        });
    }
}
