package ph.seniorchain.domain.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ph.seniorchain.domain.account.Role;
import ph.seniorchain.domain.account.UserAccount;
import ph.seniorchain.domain.account.UserAccountRepository;
import ph.seniorchain.domain.audit.AuditAction;
import ph.seniorchain.domain.audit.AuditTrail;
import ph.seniorchain.domain.ledger.LedgerService;

/** Checks that the service calls its dependencies correctly; business rules are in RegistrationTest. */
class RegistrationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T02:00:00Z");
    private static final IdHmac HMAC = new IdHmac("b".repeat(64));

    private final UserAccountRepository accounts = mock(UserAccountRepository.class);
    private final SeniorCitizenRepository citizens = mock(SeniorCitizenRepository.class);
    private final RegistrationRepository registrations = mock(RegistrationRepository.class);
    private final IdCardRepository idCards = mock(IdCardRepository.class);
    private final LedgerService ledger = mock(LedgerService.class);
    private final AuditTrail audit = mock(AuditTrail.class);
    private RegistrationService service;
    private UserAccount account;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(accounts, citizens, registrations, idCards, ledger, audit,
                Clock.fixed(NOW, ZoneOffset.UTC), 5);
        account = new UserAccount("senior@seniorchain.test", "hash", "Test Senior", Role.SENIOR_CITIZEN, NOW);
        when(accounts.findById(account.id())).thenReturn(Optional.of(account));
        when(citizens.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrations.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idCards.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static NewRegistration request(LocalDate birthDate) {
        return new NewRegistration(new PersonalDetails("Test Senior", new BirthDate(birthDate), "1 Test St", null),
                HMAC, RecordType.SC, "QC-OSCA");
    }

    @Test
    void submitSavesCitizenAndRegistrationAndAudits() {
        Registration registration = service.submit(account.id(), request(LocalDate.of(1950, 5, 5)));

        assertThat(registration.status()).isEqualTo(RegistrationStatus.SUBMITTED);
        verify(citizens).save(any(SeniorCitizen.class));
        verify(audit).record(eq(account.id()), eq(AuditAction.REGISTRATION_SUBMITTED), eq(registration.id()), isNull());
    }

    @Test
    void submitRejectsDuplicateIdNumber() {
        when(citizens.existsByIdHmac(HMAC)).thenReturn(true);

        assertThatThrownBy(() -> service.submit(account.id(), request(LocalDate.of(1950, 5, 5))))
                .isInstanceOf(DuplicateRegistrationException.class);
        verify(registrations, never()).save(any());
    }

    @Test
    void submitRejectsApplicantUnder60() {
        assertThatThrownBy(() -> service.submit(account.id(), request(LocalDate.of(1970, 1, 1))))
                .isInstanceOf(NotEligibleException.class);
        verify(citizens, never()).save(any());
    }

    @Test
    void approveIssuesCardAndQueuesLedgerWrite() {
        Registration registration = service.submit(account.id(), request(LocalDate.of(1950, 5, 5)));
        when(registrations.findWithCitizenById(registration.id())).thenReturn(Optional.of(registration));
        UUID reviewer = UUID.randomUUID();

        service.approve(registration.id(), reviewer);

        ArgumentCaptor<IdCard> card = ArgumentCaptor.forClass(IdCard.class);
        verify(idCards).save(card.capture());
        assertThat(card.getValue().validUntil()).isEqualTo(LocalDate.of(2031, 11, 10));
        assertThat(card.getValue().cardNumber()).matches("SC-2026-\\d{8}");
        verify(ledger).anchorApproval(registration, card.getValue());
        verify(audit).record(reviewer, AuditAction.REGISTRATION_APPROVED, registration.id(), null);
    }

    @Test
    void rejectDoesNotTouchLedger() {
        Registration registration = service.submit(account.id(), request(LocalDate.of(1950, 5, 5)));
        when(registrations.findWithCitizenById(registration.id())).thenReturn(Optional.of(registration));

        service.reject(registration.id(), UUID.randomUUID(), "Incomplete documents");

        verify(ledger, never()).anchorApproval(any(), any());
        verify(ledger, never()).anchorRevocation(any());
    }

    @Test
    void revokeQueuesLedgerWrite() {
        Registration registration = service.submit(account.id(), request(LocalDate.of(1950, 5, 5)));
        when(registrations.findWithCitizenById(registration.id())).thenReturn(Optional.of(registration));
        service.approve(registration.id(), UUID.randomUUID());

        service.revoke(registration.id(), UUID.randomUUID(), "Fraudulent documents");

        verify(ledger).anchorRevocation(registration);
    }
}
