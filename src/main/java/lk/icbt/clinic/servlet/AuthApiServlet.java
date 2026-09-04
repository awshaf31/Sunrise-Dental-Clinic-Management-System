package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.StaffResponse;
import lk.icbt.clinic.model.Staff;

import java.util.Map;

/**
 * POST /api/auth/login -- verifies credentials and returns identity.
 * <p>
 * The service stays stateless: this endpoint neither reads nor writes an
 * HttpSession. The session belongs to the presentation layer
 * ({@link LoginServlet}), which is what lets this endpoint be called by any
 * future client without dragging session state along with it.
 */
@WebServlet("/api/auth/login")
public class AuthApiServlet extends JsonServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handle(resp, () -> {
            @SuppressWarnings("unchecked")
            Map<String, String> body = readBody(req, Map.class);
            Staff staff = ctx().authService.authenticate(body.get("username"), body.get("password"));
            writeJson(resp, 200, new StaffResponse(staff));
        });
    }
}
