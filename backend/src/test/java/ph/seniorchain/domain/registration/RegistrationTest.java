package ph.seniorchain.domain.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ph.seniorchain.domain.account.Role;
import ph.seniorchain.domain.account.UserAccount;

class RegistrationTest {

    private static final Instant NOW = Instant.parse("2026-11-10T02:00:00Z");
    private static final UUID REVIEWER = UUID.randomUUID();

    private static Registration submitted() {
        UserAccount account = new UserAccount("a@seniorchain.test", "hash", "Test Person", Role.SENIOR_CITIZEN, NOW);
        PersonalDetails details = new PersonalDetails("Test Person", new BirthDate(LocalDate.of(1950, 1, 1)),
                "1 Test St", null);
        SeniorCitizen citizen = new SeniorCitizen(account, details, new IdHmac("a".repeat(64)), NOW);
        return Registration.submit(citizen, RecordType.SC, "QC-OSCA", NOW);
    }

    @Test
    void newRegistrationIsSubmitted() {
        assertThat(submitted().status()).isEqualTo(RegistrationStatus.SUBMITTED);
    }

    @Test
    void approveMakesItActiveAndRecordsReviewer() {
        Registration registration = submitted();

        registration.approve(REVIEWER, NOW);

        assertThat(registration.status()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(registration.isActive()).isTrue();
        assertThat(registration.reviewedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectStoresReason() {
        Registration registration = submitted();

        registration.reject(REVIEWER, "Blurry document", NOW);

        assertThat(registration.status()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(registration.reason()).isEqualTo("Blurry document");
    }

    @Test
    void rejectRequiresReason() {
        Registration registration = submitted();

        assertThatThrownBy(() -> registration.reject(REVIEWER, " ", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revokeAfterApproval() {
        Registration registration = submitted();
        registration.approve(REVIEWER, NOW);

        registration.revoke(REVIEWER, "Deceased", NOW);

        assertThat(registration.status()).isEqualTo(RegistrationStatus.REVOKED);
        assertThat(registration.isActive()).isFalse();
    }

    @Test
    void cannotApproveTwice() {
        Registration registration = submitted();
        registration.approve(REVIEWER, NOW);

        assertThatThrownBy(() -> registration.approve(REVIEWER, NOW))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }

    @Test
    void cannotApproveRejected() {
        Registration registration = submitted();
        registration.reject(REVIEWER, "Incomplete", NOW);

        assertThatThrownBy(() -> registration.approve(REVIEWER, NOW))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }

    @Test
    void cannotRevokeBeforeApproval() {
        Registration registration = submitted();

        assertThatThrownBy(() -> registration.revoke(REVIEWER, "Fraud", NOW))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }

    @Test
    void idCardOnlyForActiveRegistration() {
        Registration registration = submitted();

        assertThatThrownBy(() -> IdCard.issue(registration, "SC-2026-00000001", NOW, LocalDate.of(2031, 11, 10)))
                .isInstanceOf(IllegalStateException.class);
    }
}
