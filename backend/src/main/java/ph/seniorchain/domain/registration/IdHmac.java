package ph.seniorchain.domain.registration;

import java.util.regex.Pattern;
import ph.seniorchain.domain.common.Require;

/**
 * Keyed fingerprint of a government ID number: HMAC-SHA256, 32 bytes as 64 lowercase hex chars.
 * Used for duplicate detection in Postgres and as the record key on the blockchain.
 */
public record IdHmac(String hex) {

    private static final Pattern HEX_64 = Pattern.compile("[0-9a-f]{64}");

    public IdHmac {
        Require.notNull(hex, "idHmac");
        if (!HEX_64.matcher(hex).matches()) {
            throw new IllegalArgumentException("idHmac must be 64 lowercase hex characters");
        }
    }
}
