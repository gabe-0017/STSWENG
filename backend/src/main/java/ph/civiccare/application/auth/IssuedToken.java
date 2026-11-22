package ph.civiccare.application.auth;

import java.time.Instant;
import ph.civiccare.domain.account.Role;

public record IssuedToken(String token, Instant expiresAt, Role role) {
}
