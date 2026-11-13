package ph.seniorchain.application.auth;

import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ph.seniorchain.config.SecurityConfig;
import ph.seniorchain.config.SeniorChainProperties;
import ph.seniorchain.domain.account.UserAccount;

/** Issues the short-lived login JWT (subject = user id, plus the role claim). */
@Service
public class AccessTokenService {

    private static final String ISSUER = "seniorchain-ph";

    private final JwtEncoder encoder;
    private final SeniorChainProperties properties;
    private final Clock clock;

    public AccessTokenService(JwtEncoder encoder, SeniorChainProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedToken issue(UserAccount account) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.jwt().ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(account.id().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(SecurityConfig.ROLE_CLAIM, account.role().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt, account.role());
    }
}
