package ph.seniorchain.domain.common;

import java.util.Objects;

/** Parameter checks shared by domain classes. Failures surface as HTTP 400. */
public final class Require {

    private Require() {
    }

    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    public static String notBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.strip();
    }

    public static String maxLength(String value, int max, String name) {
        Objects.requireNonNull(value, name);
        if (value.length() > max) {
            throw new IllegalArgumentException(name + " must be at most " + max + " characters");
        }
        return value;
    }
}
