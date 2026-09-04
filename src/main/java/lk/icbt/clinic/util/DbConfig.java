package lk.icbt.clinic.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Resolves database configuration, checked in this order:
 * <ol>
 *   <li>a real environment variable ({@code DB_USERNAME}, {@code DB_PASSWORD}, {@code DB_URL})</li>
 *   <li>{@code local.properties} in the project root, if present</li>
 * </ol>
 * <p>
 * The environment variable always wins, which is what a real deployment (or
 * the CI pipeline) should use. {@code local.properties} exists purely so
 * running from an IDE does not depend on that IDE's Run Configuration
 * dialog having been filled in correctly -- a step that turned out, in
 * practice, to be an easy place to go wrong and hard for anyone else to
 * debug without seeing the screen. The file is listed in {@code .gitignore}
 * and must never be committed: it is exactly the kind of credential this
 * project otherwise takes care never to hard-code.
 * <p>
 * Example {@code local.properties}, next to {@code pom.xml}:
 * <pre>
 *   DB_USERNAME=root
 *   DB_PASSWORD=root123
 * </pre>
 */
public final class DbConfig {

    private static final Properties LOCAL = loadLocalProperties();

    private DbConfig() {
    }

    public static String get(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return LOCAL.getProperty(key);
    }

    private static Properties loadLocalProperties() {
        Properties props = new Properties();
        Path path = Path.of(System.getProperty("user.dir"), "local.properties");
        if (Files.exists(path)) {
            try (FileInputStream in = new FileInputStream(path.toFile())) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read local.properties: " + e.getMessage());
            }
        }
        return props;
    }
}
