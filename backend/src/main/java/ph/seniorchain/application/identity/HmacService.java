package ph.seniorchain.application.identity;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import ph.seniorchain.config.SeniorChainProperties;
import ph.seniorchain.domain.registration.GovIdNumber;
import ph.seniorchain.domain.registration.IdHmac;

/**
 * idHmac = HMAC-SHA256(secret, normalizedGovIdNumber). A plain hash would be unsafe because
 * ID numbers are few enough to brute-force; without the secret key the HMAC cannot be recomputed.
 */
@Service
public class HmacService {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec key;

    public HmacService(SeniorChainProperties properties) {
        this.key = new SecretKeySpec(properties.idHmac().secret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public IdHmac fingerprint(GovIdNumber idNumber) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            byte[] digest = mac.doFinal(idNumber.value().getBytes(StandardCharsets.UTF_8));
            return new IdHmac(HexFormat.of().formatHex(digest));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}
