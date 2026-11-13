package ph.seniorchain.web.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import ph.seniorchain.domain.ledger.LedgerSyncStatus;
import ph.seniorchain.domain.registration.IdCard;
import ph.seniorchain.domain.registration.RecordType;
import ph.seniorchain.domain.registration.Registration;
import ph.seniorchain.domain.registration.RegistrationStatus;
import ph.seniorchain.domain.registration.SeniorCitizen;

/** Request and response bodies for /api/v1/registrations. */
public final class RegistrationDtos {

    private RegistrationDtos() {
    }

    /** Senior Citizen ID application. govIdNumber is only fingerprinted, never stored. */
    public record SubmitRegistrationRequest(
            @NotBlank @Size(max = 200) String fullName,
            @NotNull @Past LocalDate birthDate,
            @NotBlank @Size(max = 500) String address,
            @Size(max = 30) String contactNumber,
            @NotBlank @Size(max = 60) String govIdNumber,
            @NotBlank @Size(max = 20) String lguCode) {

        @Override
        public String toString() {
            return "SubmitRegistrationRequest[hidden]";
        }
    }

    public record ReasonRequest(@NotBlank @Size(max = 500) String reason) {
    }

    /** One row of the LGU review queue. */
    public record RegistrationSummary(UUID id, String fullName, RecordType recordType, RegistrationStatus status,
                                      String lguCode, Instant submittedAt) {

        public static RegistrationSummary from(Registration r) {
            return new RegistrationSummary(r.id(), r.citizen().fullName(), r.recordType(), r.status(),
                    r.lguCode(), r.submittedAt());
        }
    }

    public record Applicant(String fullName, LocalDate birthDate, String address, String contactNumber) {

        static Applicant from(SeniorCitizen c) {
            return new Applicant(c.fullName(), c.birthDate().value(), c.address(), c.contactNumber());
        }
    }

    public record IdCardInfo(String cardNumber, Instant issuedAt, LocalDate validUntil) {

        static IdCardInfo from(IdCard card) {
            return new IdCardInfo(card.cardNumber(), card.issuedAt(), card.validUntil());
        }
    }

    /**
     * Full view for the LGU reviewer, government admin or the registrant.
     * idCard and ledgerStatus are null until the registration is approved.
     * ledgerStatus PENDING means "Approved – pending on-chain".
     */
    public record RegistrationDetail(UUID id, RecordType recordType, RegistrationStatus status, String lguCode,
                                     Instant submittedAt, Instant reviewedAt, String reason, Applicant applicant,
                                     IdCardInfo idCard, LedgerSyncStatus ledgerStatus) {
    }
}
