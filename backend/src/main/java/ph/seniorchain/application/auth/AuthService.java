package ph.seniorchain.application.auth;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.seniorchain.domain.account.Role;
import ph.seniorchain.domain.account.UserAccount;
import ph.seniorchain.domain.account.UserAccountRepository;
import ph.seniorchain.domain.common.ConflictException;
import ph.seniorchain.domain.common.NotFoundException;

/**
 * Login and self sign-up. Only senior citizens can sign up themselves; LGU, establishment and
 * government accounts are created by an admin (seeded in dev/staging).
 */
@Service
public class AuthService {

    private static final String INVALID_LOGIN = "Invalid email or password";

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService tokens;
    private final Clock clock;
    /** Compared against when the email is unknown, so both cases take the same time. */
    private final String dummyHash;

    public AuthService(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                       AccessTokenService tokens, Clock clock) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public IssuedToken login(String email, String password) {
        Optional<UserAccount> account = accounts.findByEmail(UserAccount.normalizeEmail(email));
        String hash = account.map(UserAccount::passwordHash).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(password, hash);
        if (account.isEmpty() || !matches || !account.get().isEnabled()) {
            throw new BadCredentialsException(INVALID_LOGIN);
        }
        return tokens.issue(account.get());
    }

    @Transactional
    public UserAccount signUpSeniorCitizen(String email, String password, String fullName) {
        return createAccount(email, password, fullName, Role.SENIOR_CITIZEN);
    }

    /** Used by admins and the demo seeder. */
    @Transactional
    public UserAccount createAccount(String email, String password, String fullName, Role role) {
        String normalized = UserAccount.normalizeEmail(email);
        if (accounts.existsByEmail(normalized)) {
            throw new ConflictException("An account with this email already exists");
        }
        return accounts.save(new UserAccount(normalized, passwordEncoder.encode(password), fullName, role,
                clock.instant()));
    }

    @Transactional(readOnly = true)
    public UserAccount get(UUID accountId) {
        return accounts.findById(accountId).orElseThrow(() -> new NotFoundException("Account not found"));
    }
}
