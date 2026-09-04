package lk.icbt.clinic.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.icbt.clinic.exception.AuthenticationFailedException;
import lk.icbt.clinic.filter.AuthenticationFilter;
import lk.icbt.clinic.model.Staff;

import java.io.IOException;

/**
 * GET/POST /login.
 * <p>
 * On success, the old session is invalidated and a new one issued before the
 * authenticated user is stored in it -- so a session id observed before login
 * cannot be reused to ride the authenticated session.
 */
@WebServlet("/login")
public class LoginServlet extends ViewServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (currentUser(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        if (req.getParameter("expired") != null) {
            req.setAttribute("notice", "Your session ended. Please sign in again.");
        }
        render(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            Staff staff = ctx().authService.authenticate(username, password);

            HttpSession old = req.getSession(false);
            String redirect = old == null ? null
                    : (String) old.getAttribute("clinic.redirectAfterLogin");
            if (old != null) old.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute(AuthenticationFilter.SESSION_USER_KEY, staff);

            resp.sendRedirect(req.getContextPath()
                    + (redirect == null || redirect.startsWith("/login") ? "/" : redirect));

        } catch (AuthenticationFailedException e) {
            req.setAttribute("username", username);
            req.setAttribute("error", e.getMessage());
            render(req, resp, "login.jsp");
        }
    }
}
