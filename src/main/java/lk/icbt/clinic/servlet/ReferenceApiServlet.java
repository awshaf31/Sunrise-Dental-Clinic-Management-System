package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.DentistResponse;
import lk.icbt.clinic.dto.TreatmentTypeResponse;
import lk.icbt.clinic.exception.InvalidBookingException;

/** Lookup data the booking form needs to populate its dropdowns. */
@WebServlet("/api/reference/*")
public class ReferenceApiServlet extends JsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            String which = pathParam(req);
            switch (which) {
                case "dentists" -> writeJson(resp, 200, ctx().dentistDao.findAllActive().stream()
                        .map(DentistResponse::new).toList());
                case "treatments" -> writeJson(resp, 200, ctx().treatmentTypeDao.findAllActive().stream()
                        .map(TreatmentTypeResponse::new).toList());
                default -> throw new InvalidBookingException("Unknown reference data: " + which);
            }
        });
    }
}
