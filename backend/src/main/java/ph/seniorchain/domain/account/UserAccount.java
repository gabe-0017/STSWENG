package ph.seniorchain.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import ph.seniorchain.domain.common.Require;

/** A login account for any role. */
@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    protected UserAccount() {
    }

    public UserAccount(String email, String passwordHash, String fullName, Role role, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.email = normalizeEmail(email);
        this.passwordHash = Require.notBlank(passwordHash, "passwordHash");
        this.fullName = Require.maxLength(Require.notBlank(fullName, "fullName"), 200, "fullName");
        this.role = Require.notNull(role, "role");
        this.createdAt = Require.notNull(createdAt, "createdAt");
        this.enabled = true;
    }

    public static String normalizeEmail(String email) {
        return Require.maxLength(Require.notBlank(email, "email"), 254, "email").toLowerCase(Locale.ROOT);
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String fullName() {
        return fullName;
    }

    public Role role() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof UserAccount other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        // No email or name: personal data stays out of logs
        return "UserAccount[id=" + id + ", role=" + role + "]";
    }
}
