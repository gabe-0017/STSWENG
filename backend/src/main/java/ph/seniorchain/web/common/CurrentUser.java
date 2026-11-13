package ph.seniorchain.web.common;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/** Reads the caller's account id from the JWT subject. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
