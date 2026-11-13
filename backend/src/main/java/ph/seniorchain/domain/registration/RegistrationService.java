package ph.seniorchain.domain.registration;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.seniorchain.domain.account.UserAccount;
import ph.seniorchain.domain.account.UserAccountRepository;
import ph.seniorchain.domain.audit.AuditAction;
import ph.seniorchain.domain.audit.AuditTrail;
import ph.seniorchain.domain.common.NotFoundException;
import ph.seniorchain.domain.ledger.LedgerService;
import ph.seniorchain.domain.ledger.LedgerSyncStatus;

/**
 * Registration workflow: submit, approve, reject, revoke.
 * Each state change, its ledger (outbox) entry and its audit entry are saved in one transaction.
 */
@Service
public class RegistrationService {

    /** Dates on IDs follow Philippine time. */
    public static final ZoneId PH_ZONE = ZoneId.of("Asia/Manila");

    private final UserAccountRepository accounts;
    private final SeniorCitizenRepository citizens;
    private final RegistrationRepository registrations;
    private final IdCardRepository idCards;
    private final LedgerService ledger;
    private final AuditTrail audit;
    private final Clock clock;
    private final int idValidityYears;
    private final SecureRandom random = new SecureRandom();

    public RegistrationService(UserAccountRepository accounts,
                               SeniorCitizenRepository citizens,
                               RegistrationRepository registrations,
                               IdCardRepository idCards,
                               LedgerService ledger,
                               AuditTrail audit,
                               Clock clock,
                               @Value("${seniorchain.id-card.validity-years}") int idValidityYears) {
        this.accounts = accounts;
        this.citizens = citizens;
        this.registrations = registrations;
        this.idCards = idCards;
        this.ledger = ledger;
        this.audit = audit;
        this.clock = clock;
        this.idValidityYears = idValidityYears;
    }

    @Transactional
    public Registration submit(UUID accountId, NewRegistration request) {
        UserAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (citizens.existsByAccountId(accountId) || citizens.existsByIdHmac(request.idHmac())) {
            throw new DuplicateRegistrationException();
        }
        if (request.recordType() == RecordType.SC && !request.details().birthDate().isSeniorOn(today())) {
            throw new NotEligibleException(
                    "Applicant must be at least " + BirthDate.SENIOR_AGE + " years old for a Senior Citizen ID");
        }

        Instant now = clock.instant();
        SeniorCitizen citizen = citizens.save(new SeniorCitizen(account, request.details(), request.idHmac(), now));
        Registration registration = registrations.save(
                Registration.submit(citizen, request.recordType(), request.lguCode(), now));
        audit.record(accountId, AuditAction.REGISTRATION_SUBMITTED, registration.id(), null);
        return registration;
    }

    @Transactional
    public Registration approve(UUID registrationId, UUID reviewerId) {
        Registration registration = load(registrationId);
        Instant now = clock.instant();
        registration.approve(reviewerId, now);

        IdCard card = idCards.save(IdCard.issue(
                registration, newCardNumber(registration.recordType()), now, today().plusYears(idValidityYears)));
        ledger.anchorApproval(registration, card);
        audit.record(reviewerId, AuditAction.REGISTRATION_APPROVED, registrationId, null);
        return registration;
    }

    @Transactional
    public Registration reject(UUID registrationId, UUID reviewerId, String reason) {
        Registration registration = load(registrationId);
        registration.reject(reviewerId, reason, clock.instant());
        audit.record(reviewerId, AuditAction.REGISTRATION_REJECTED, registrationId, registration.reason());
        return registration;
    }

    @Transactional
    public Registration revoke(UUID registrationId, UUID actorId, String reason) {
        Registration registration = load(registrationId);
        registration.revoke(actorId, reason, clock.instant());
        ledger.anchorRevocation(registration);
        audit.record(actorId, AuditAction.REGISTRATION_REVOKED, registrationId, registration.reason());
        return registration;
    }

    @Transactional(readOnly = true)
    public Page<Registration> list(Optional<RegistrationStatus> status, Pageable pageable) {
        return status.map(s -> registrations.findByStatus(s, pageable))
                .orElseGet(() -> registrations.findAllBy(pageable));
    }

    @Transactional(readOnly = true)
    public Registration get(UUID registrationId) {
        return load(registrationId);
    }

    @Transactional(readOnly = true)
    public Optional<Registration> findLatestForAccount(UUID accountId) {
        return registrations.findFirstByCitizenAccountIdOrderBySubmittedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public Optional<IdCard> idCardFor(UUID registrationId) {
        return idCards.findByRegistrationId(registrationId);
    }

    @Transactional(readOnly = true)
    public Optional<LedgerSyncStatus> ledgerStatus(UUID registrationId) {
        return ledger.syncStatus(registrationId);
    }

    private Registration load(UUID registrationId) {
        return registrations.findWithCitizenById(registrationId)
                .orElseThrow(() -> new NotFoundException("Registration not found"));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(PH_ZONE));
    }

    /** e.g. SC-2026-04812345. Uniqueness is enforced by a database constraint. */
    private String newCardNumber(RecordType type) {
        return "%s-%d-%08d".formatted(type, today().getYear(), random.nextInt(100_000_000));
    }
}
