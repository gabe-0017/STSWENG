package ph.civiccare.domain.registration;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.civiccare.domain.account.UserAccount;
import ph.civiccare.domain.account.UserAccountRepository;
import ph.civiccare.domain.audit.AuditAction;
import ph.civiccare.domain.audit.AuditTrail;
import ph.civiccare.domain.common.NotFoundException;
import ph.civiccare.domain.ledger.LedgerService;
import ph.civiccare.domain.ledger.LedgerSyncStatus;

/**
 * Registration workflow: submit, approve, reject, revoke.
 * Each state change, its ledger (outbox) entry and its audit entry are saved in one transaction.
 */
@Service
public class RegistrationService {

    /** Dates on IDs follow Philippine time. */
    public static final ZoneId PH_ZONE = ZoneId.of("Asia/Manila");

    /** A person may hold one registration of each type in these states at a time. */
    private static final Set<RegistrationStatus> OPEN_STATUSES = EnumSet.of(
            RegistrationStatus.SUBMITTED, RegistrationStatus.APPROVED, RegistrationStatus.AMENDED);

    private final UserAccountRepository accounts;
    private final CitizenRepository citizens;
    private final RegistrationRepository registrations;
    private final IdCardRepository idCards;
    private final LedgerService ledger;
    private final AuditTrail audit;
    private final Clock clock;
    private final int idValidityYears;
    private final SecureRandom random = new SecureRandom();

    public RegistrationService(UserAccountRepository accounts,
                               CitizenRepository citizens,
                               RegistrationRepository registrations,
                               IdCardRepository idCards,
                               LedgerService ledger,
                               AuditTrail audit,
                               Clock clock,
                               @Value("${civiccare.id-card.validity-years}") int idValidityYears) {
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
        Citizen citizen = citizens.findByAccountId(accountId).orElse(null);
        // A returning citizen is judged on the birth date already on file, not on a new one in the request.
        BirthDate birthDate = citizen != null ? citizen.birthDate() : request.details().birthDate();
        if (request.recordType() == RecordType.SC && !birthDate.isSeniorOn(today())) {
            throw new NotEligibleException(
                    "Applicant must be at least " + BirthDate.SENIOR_AGE + " years old for a Senior Citizen ID");
        }

        Instant now = clock.instant();
        if (citizen == null) {
            // First registration for this account: the government ID must not belong to anyone else.
            if (citizens.existsByIdHmac(request.idHmac())) {
                throw new DuplicateRegistrationException();
            }
            citizen = citizens.save(new Citizen(account, request.details(), request.idHmac(), now));
        } else if (!citizen.idHmac().equals(request.idHmac())) {
            // Later registrations (e.g. PWD after SC) must be for the same person.
            throw new DuplicateRegistrationException();
        }
        if (registrations.existsByCitizenIdAndRecordTypeAndStatusIn(
                citizen.id(), request.recordType(), OPEN_STATUSES)) {
            throw new DuplicateRegistrationException();
        }

        Registration registration = registrations.save(Registration.submit(
                citizen, request.recordType(), request.disabilityClassification(), request.lguCode(), now));
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
    public List<Registration> findAllForAccount(UUID accountId) {
        return registrations.findByCitizenAccountIdOrderBySubmittedAtDesc(accountId);
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
