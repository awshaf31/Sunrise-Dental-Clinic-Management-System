package lk.icbt.clinic.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.icbt.clinic.model.Staff;

import java.io.IOException;

/**
 * Refuses every request that has no authenticated session.
 * <p>
 * Applied as a default-deny rule to every path ({@code /*}), with an
 * explicit allow-list for what must be reachable while logged out. Forgetting
 * to protect a new page is a far more likely mistake than forgetting to
 * exempt one, so the filter is written to fail closed.
 * <p>
 * The {@code @WebFilter} annotation registers this the same way {@code web.xml}
 * would; it is a Jakarta Servlet API mechanism, not a third-party framework
 * intercepting the request before it reaches this class.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    public static final String SESSION_USER_KEY = "clinic.user";
    private static final String REDIRECT_KEY = "clinic.redirectAfterLogin";

    private static final String[] PUBLIC_PATHS = {
            "/login", "/logout", "/css/", "/js/", "/api/auth/login", "/favicon.ico",
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(SESSION_USER_KEY) instanceof Staff) {
            chain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/")) {
            // A JSON client gets a clean 401, not an HTML redirect.
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("""
                    {"status":401,"error":"Unauthorized","message":"Sign in required"}""");
            return;
        }

        // Remember where the user was headed so login can return them there.
        HttpSession newSession = req.getSession(true);
        String target = req.getRequestURI()
                + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
        newSession.setAttribute(REDIRECT_KEY, target);

        resp.sendRedirect(req.getContextPath() + "/login?expired");
    }

    private boolean isPublic(String path) {
        for (String prefix : PUBLIC_PATHS) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
