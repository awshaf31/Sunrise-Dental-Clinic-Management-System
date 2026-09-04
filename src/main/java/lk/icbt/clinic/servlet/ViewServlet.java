package lk.icbt.clinic.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.icbt.clinic.exception.*;
import lk.icbt.clinic.filter.AuthenticationFilter;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.util.AppContext;
import lk.icbt.clinic.util.AppInitializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Shared plumbing for servlets that render a JSP view. */
public abstract class ViewServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ViewServlet.class.getName());

    protected AppContext ctx() {
        return AppInitializer.get(getServletContext());
    }

    protected Staff currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Staff) session.getAttribute(AuthenticationFilter.SESSION_USER_KEY);
    }

    protected void render(HttpServletRequest req, HttpServletResponse resp, String jsp) {
        try {
            RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/jsp/" + jsp);
            dispatcher.forward(req, resp);
        } catch (ServletException | IOException e) {
            throw new UncheckedIOException("Failed to render " + jsp, new IOException(e));
        }
    }

    /**
     * Runs a request handler and, on a business exception, renders the
     * shared error page rather than letting a stack trace reach the user.
     */
    protected void handle(HttpServletRequest req, HttpServletResponse resp, Runnable action) {
        try {
            action.run();
        } catch (AppointmentNotFoundException e) {
            req.setAttribute("title", "Not found");
            req.setAttribute("message", e.getMessage());
            render(req, resp, "error.jsp");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Unhandled exception rendering page", e);
            req.setAttribute("title", "Something went wrong");
            req.setAttribute("message",
                    "The system could not complete that request. Please try again.");
            render(req, resp, "error.jsp");
        }
    }
}
