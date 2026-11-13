package ph.seniorchain.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** All "seniorchain.*" settings. Startup fails if a required value is missing or too weak. */
@Validated
@ConfigurationProperties("seniorchain")
public record SeniorChainProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull IdHmacSettings idHmac,
        @Valid @NotNull Cors cors,
        @Valid @NotNull IdCardSettings idCard,
        @Valid @NotNull DemoData demoData) {

    /** signingKey must be at least 32 characters (256 bits) for HS256. */
    public record Jwt(@NotNull @Size(min = 32) String signingKey, @NotNull Duration ttl) {
    }

    public record IdHmacSettings(@NotNull @Size(min = 32) String secret) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }

    public record IdCardSettings(@Min(1) int validityYears) {
    }

    /** Seeds synthetic demo accounts on startup. Skipped when password is blank. */
    public record DemoData(boolean enabled, String password) {
    }
}
