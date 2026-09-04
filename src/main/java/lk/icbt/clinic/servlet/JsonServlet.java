package lk.icbt.clinic.servlet;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.icbt.clinic.dto.ErrorResponse;
import lk.icbt.clinic.exception.*;
import lk.icbt.clinic.util.AppContext;
import lk.icbt.clinic.util.AppInitializer;
import lk.icbt.clinic.util.GsonProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared plumbing for the JSON web-service endpoints.
 * <p>
 * Everything here is what Spring's {@code @RestControllerAdvice} and message
 * converters do automatically: reading the request body into an object,
 * writing the response as JSON with the right content type, and mapping a
 * business exception to the right HTTP status. Without a framework, that
 * mapping is one method, in one place, that every servlet calls.
 */
public abstract class JsonServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    protected final Gson gson = GsonProvider.get();

    protected AppContext ctx() {
        return AppInitializer.get(getServletContext());
    }

    protected <T> T readBody(HttpServletRequest req, Class<T> type) {
        try {
            return gson.fromJson(req.getReader(), type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read request body", e);
        }
    }

    protected void writeJson(HttpServletResponse resp, int status, Object body) {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        try {
            resp.getWriter().write(gson.toJson(body));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write JSON response", e);
        }
    }

    /**
     * Runs a request handler and turns any business exception into the right
     * HTTP status and one consistent error body -- the equivalent of
     * Spring's {@code @ExceptionHandler} methods, called explicitly instead
     * of being wired in by a container.
     */
    protected void handle(HttpServletResponse resp, Runnable action) {
        try {
            action.run();
        } catch (InvalidBookingException e) {
            writeJson(resp, 400, new ErrorResponse(400, "Invalid Booking", e.getMessage(), null));
        } catch (SlotUnavailableException e) {
            // 409, not 400: the request was well formed, it lost the slot.
            writeJson(resp, 409, new ErrorResponse(409, "Slot Unavailable", e.getMessage(), null));
        } catch (AppointmentNotFoundException e) {
            writeJson(resp, 404, new ErrorResponse(404, "Not Found", e.getMessage(), null));
        } catch (AuthenticationFailedException e) {
            writeJson(resp, 401, new ErrorResponse(401, "Authentication Failed", e.getMessage(), null));
        } catch (ReportUnavailableException e) {
            writeJson(resp, 503, new ErrorResponse(503, "Report Unavailable", e.getMessage(), null));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unhandled exception serving API request", e);
            writeJson(resp, 500, new ErrorResponse(500, "Internal Server Error",
                    "Something went wrong. Please contact the system administrator.", null));
        }
    }

    protected Long staffIdHeader(HttpServletRequest req) {
        String header = req.getHeader("X-Staff-Id");
        if (header == null || header.isBlank()) {
            throw new InvalidBookingException("X-Staff-Id header is required");
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new InvalidBookingException("X-Staff-Id must be numeric");
        }
    }

    /** Last, non-empty path segment after the servlet path -- this servlet's stand-in for @PathVariable. */
    protected String pathParam(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new InvalidBookingException("A path parameter is required");
        }
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int slash = trimmed.indexOf('/');
        return slash == -1 ? trimmed : trimmed.substring(0, slash);
    }
}
