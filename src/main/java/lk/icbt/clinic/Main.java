package lk.icbt.clinic;

import lk.icbt.clinic.util.DbConfig;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs this application from an IDE with a single click, with no server
 * installation and no deployment step.
 * <p>
 * This is a developer convenience, not part of the deployed system. It
 * exists because IntelliJ IDEA Community Edition -- unlike Ultimate -- has
 * no built-in "run a WAR against Tomcat" configuration; embedding Tomcat
 * behind an ordinary {@code main()} method sidesteps that entirely and works
 * identically in any IDE, or from the command line with
 * {@code mvn compile exec:java}.
 * <p>
 * Tomcat itself is still not a framework in the sense the rest of this
 * project avoids: nothing here performs dependency injection, intercepts
 * method calls, or manages the lifecycle of application objects the way
 * Spring's container would. It serves HTTP and dispatches to servlets,
 * which is exactly what the {@code jakarta.servlet-api} contract describes.
 * <p>
 * The {@code tomcat-embed-*} dependencies this class needs sit on the
 * normal compile classpath (see {@code pom.xml} for why {@code provided}
 * scope did not work reliably from an IDE run configuration), but are
 * explicitly excluded from the packaged WAR by {@code maven-war-plugin}'s
 * {@code packagingExcludes} -- the deployable artifact is unaffected, and
 * still expects a real, separately installed Tomcat.
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        quietTomcatLogging();
        requireDatabaseCredentials();

        String workingDir = System.getProperty("user.dir");
        String webappDir = workingDir + "/src/main/webapp";
        String classesDir = workingDir + "/target/classes";

        if (!new File(classesDir).exists()) {
            System.err.println();
            System.err.println("target/classes does not exist yet.");
            System.err.println("Build the project first: Build > Build Project (or mvn compile).");
            System.err.println();
            System.exit(1);
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(workingDir + "/target/tomcat-embedded");
        tomcat.getConnector(); // forces connector creation before addWebapp

        Context context = tomcat.addWebapp("", new File(webappDir).getAbsolutePath());

        // addWebapp() only knows about src/main/webapp; it has no idea the
        // compiled classes live in target/classes rather than
        // WEB-INF/classes, since this is running straight from source
        // instead of from a packaged WAR. This adds that directory
        // explicitly so the servlets, filters and listener are found.
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(
                resources, "/WEB-INF/classes", classesDir, "/"));
        context.setResources(resources);

        tomcat.start();

        String banner = """

                ================================================================
                  Sunrise Dental Clinic is running

                    http://localhost:%d/login

                    reception / Recept@123   (receptionist)
                    manager   / Manager@123  (manager)

                  Press the red Stop button (or Ctrl+C) to shut down.
                ================================================================
                """.formatted(PORT);
        System.out.println(banner);
        System.out.flush();

        tomcat.getServer().await();
    }

    /**
     * Tomcat's own startup logging is written through java.util.logging at
     * INFO level, which is meant for a server's own log file, not for a
     * short-lived IDE Run console -- left alone, it buries the one line
     * that actually matters (the URL to open) under a dozen lines about
     * protocol handlers and TLD scanning. This turns it down to warnings
     * and above, so the console shows this application's own output and
     * nothing else, unless something has actually gone wrong.
     */
    private static void quietTomcatLogging() {
        for (String logger : new String[] {
                "org.apache.catalina", "org.apache.coyote", "org.apache.tomcat", "org.apache.jasper",
                // TldScanner sets its own level explicitly rather than
                // inheriting from "org.apache.jasper", so it needs naming
                // directly or its one INFO line survives the loop above.
                "org.apache.jasper.servlet.TldScanner",
        }) {
            Logger.getLogger(logger).setLevel(Level.WARNING);
        }

        // Logger-level filtering alone did not suppress every line in
        // testing -- some of Tomcat's loggers publish to the root logger's
        // handlers regardless, so the handlers themselves need the same
        // threshold. Belt and braces: both must agree before a record is
        // actually printed.
        for (var handler : Logger.getLogger("").getHandlers()) {
            handler.setLevel(Level.WARNING);
        }
    }

    /**
     * Fails fast and clearly rather than letting the first login attempt
     * produce an opaque "Public Key Retrieval is not allowed" JDBC stack
     * trace with no hint about what to actually do.
     * <p>
     * Checked through {@link DbConfig}, which also accepts a
     * {@code local.properties} file next to {@code pom.xml} -- getting an
     * IDE's Run Configuration dialog to actually pass an environment
     * variable through correctly has proven to be a surprisingly easy
     * step to get wrong.
     */
    private static void requireDatabaseCredentials() {
        String password = DbConfig.get("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            System.err.println();
            System.err.println("DB_PASSWORD is not set.");
            System.err.println();
            System.err.println("Easiest fix: create a file named local.properties in the project");
            System.err.println("root (next to pom.xml) containing:");
            System.err.println();
            System.err.println("    DB_USERNAME=clinic_app");
            System.err.println("    DB_PASSWORD=<the password from db/00-setup.sql>");
            System.err.println();
            System.err.println("That file is in .gitignore and is never committed. Alternatively,");
            System.err.println("set the same two as environment variables in IntelliJ under");
            System.err.println("Run > Edit Configurations > Main > Environment variables.");
            System.err.println();
            System.exit(1);
        }
    }
}
