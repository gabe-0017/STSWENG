package ph.seniorchain.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import ph.seniorchain.application.auth.IssuedToken;
import ph.seniorchain.domain.account.Role;
import ph.seniorchain.domain.account.UserAccount;

/** Request and response bodies for /api/v1/auth and /api/v1/me. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    /** BCrypt only uses the first 72 bytes of a password, hence the upper limit. */
    public record SignupRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 200) String fullName) {
    }

    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt, Role role) {

        public static TokenResponse from(IssuedToken token) {
            return new TokenResponse(token.token(), "Bearer", token.expiresAt(), token.role());
        }
    }

    public record AccountResponse(UUID id, String email, String fullName, Role role) {

        public static AccountResponse from(UserAccount account) {
            return new AccountResponse(account.id(), account.email(), account.fullName(), account.role());
        }
    }
}
