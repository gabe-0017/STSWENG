package ph.seniorchain.domain.registration;

import java.util.Locale;
import java.util.regex.Pattern;
import ph.seniorchain.domain.common.Require;

/**
 * A government ID number, normalized so the same ID always gives the same fingerprint.
 * It is only used to compute an {@link IdHmac} and is never stored.
 */
public record GovIdNumber(String value) {

    private static final Pattern SEPARATORS = Pattern.compile("[\\s-]");
    private static final Pattern VALID = Pattern.compile("[A-Z0-9]{4,40}");

    public GovIdNumber {
        Require.notNull(value, "govIdNumber");
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("govIdNumber must be 4-40 letters or digits");
        }
    }

    /** Strips spaces and dashes and uppercases, e.g. "ab-12 34" becomes "AB1234". */
    public static GovIdNumber of(String raw) {
        String trimmed = Require.notBlank(raw, "govIdNumber");
        return new GovIdNumber(SEPARATORS.matcher(trimmed).replaceAll("").toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "GovIdNumber[hidden]";
    }
}
