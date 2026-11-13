package ph.seniorchain.application.auth;

import java.time.Instant;
import ph.seniorchain.domain.account.Role;

public record IssuedToken(String token, Instant expiresAt, Role role) {
}
