package ph.seniorchain.domain.registration;

/**
 * SUBMITTED -> APPROVED -> (AMENDED) -> EXPIRED | REVOKED, or SUBMITTED -> REJECTED.
 * AMENDED and EXPIRED are reserved for later sprints.
 */
public enum RegistrationStatus {
    SUBMITTED,
    APPROVED,
    AMENDED,
    REJECTED,
    REVOKED,
    EXPIRED;

    /** An approved ID that still counts as valid (before expiry-date checks). */
    public boolean isActive() {
        return this == APPROVED || this == AMENDED;
    }
}
