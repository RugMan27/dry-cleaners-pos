package xyz.rugman27.drycleanerspos.utilites;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Utility class for loading environment variables from a `.env` file
 * with optional fallback to system environment variables.
 */
public class EnvUtils {

    private static final String DEFAULT_ENV_PATH = ".env";
    private static final HashMap<String, String> envMap = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Loads the .env file from the default path (`.env`).
     */
    public static void load() {
        load(DEFAULT_ENV_PATH);
    }

    /**
     * Loads the .env file from a custom path.
     *
     * @param path the path to the .env file
     */
    public static void load(String path) {
        if (loaded) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || !line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim().replaceAll("^\"|\"$", ""); // Remove optional quotes
                envMap.put(key, value);
            }
            loaded = true;
        } catch (IOException e) {
            System.err.println("Warning: Failed to load .env file at " + path + ": " + e.getMessage());
        }
    }

    /**
     * Gets an environment variable by key.
     * Checks the .env file first, then system environment variables.
     *
     * @param key the name of the environment variable
     * @return the value or null if not found
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Gets an environment variable with a fallback default value.
     *
     * @param key the name of the variable
     * @param defaultValue the fallback value if not found
     * @return the value of the variable, or defaultValue
     */
    public static String get(String key, String defaultValue) {
        String value = envMap.getOrDefault(key, System.getenv(key));
        return value != null ? value : defaultValue;
    }

    /**
     * Returns true if the given key is defined in either .env or system environment.
     *
     * @param key the variable name
     * @return true if the variable exists
     */
    public static boolean contains(String key) {
        return envMap.containsKey(key) || System.getenv().containsKey(key);
    }
}
