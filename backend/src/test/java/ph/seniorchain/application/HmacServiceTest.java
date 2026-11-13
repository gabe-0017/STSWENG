package ph.seniorchain.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import ph.seniorchain.application.identity.HmacService;
import ph.seniorchain.config.SeniorChainProperties;
import ph.seniorchain.domain.registration.GovIdNumber;

class HmacServiceTest {

    private static HmacService withSecret(String secret) {
        return new HmacService(new SeniorChainProperties(
                new SeniorChainProperties.Jwt("x".repeat(32), Duration.ofHours(1)),
                new SeniorChainProperties.IdHmacSettings(secret),
                new SeniorChainProperties.Cors(List.of("http://localhost")),
                new SeniorChainProperties.IdCardSettings(5),
                new SeniorChainProperties.DemoData(false, null)));
    }

    private final HmacService service = withSecret("s".repeat(32));

    @Test
    void sameIdWrittenDifferentlyGivesSameFingerprint() {
        assertThat(service.fingerprint(GovIdNumber.of("1234-5678-9012")))
                .isEqualTo(service.fingerprint(GovIdNumber.of("1234 5678 9012")));
    }

    @Test
    void differentIdsGiveDifferentFingerprints() {
        assertThat(service.fingerprint(GovIdNumber.of("123456789012")))
                .isNotEqualTo(service.fingerprint(GovIdNumber.of("123456789013")));
    }

    @Test
    void fingerprintDependsOnSecretKey() {
        GovIdNumber id = GovIdNumber.of("123456789012");

        assertThat(withSecret("t".repeat(32)).fingerprint(id)).isNotEqualTo(service.fingerprint(id));
    }

    @Test
    void matchesIndependentHmacSha256Value() {
        // Expected value from Python: hmac.new(b'k'*32, b'ABC123', hashlib.sha256).hexdigest()
        assertThat(withSecret("k".repeat(32)).fingerprint(GovIdNumber.of("abc-123")).hex())
                .isEqualTo("f6e745430af4de4d87da6e34b9b032f4269609de68cd1075ca70e9685d2c6f72");
    }
}
