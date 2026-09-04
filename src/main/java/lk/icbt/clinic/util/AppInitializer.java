package lk.icbt.clinic.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Builds the {@link AppContext} once when the web application starts, and
 * makes it available to every servlet and filter through the
 * {@code ServletContext}.
 * <p>
 * {@code @WebListener} is a Jakarta Servlet API annotation, part of the
 * platform specification Tomcat implements -- registering a listener this
 * way is no more "using a framework" than implementing {@code Runnable} is.
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    public static final String CONTEXT_KEY = "appContext";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppContext context = new AppContext();
        sce.getServletContext().setAttribute(CONTEXT_KEY, context);
    }

    public static AppContext get(jakarta.servlet.ServletContext servletContext) {
        return (AppContext) servletContext.getAttribute(CONTEXT_KEY);
    }
}
