package ph.seniorchain.web.registration;

import org.springframework.stereotype.Component;
import ph.seniorchain.domain.registration.Registration;
import ph.seniorchain.domain.registration.RegistrationService;
import ph.seniorchain.web.registration.RegistrationDtos.Applicant;
import ph.seniorchain.web.registration.RegistrationDtos.IdCardInfo;
import ph.seniorchain.web.registration.RegistrationDtos.RegistrationDetail;

/** Builds the detail response, adding the ID card and ledger status. */
@Component
public class RegistrationViews {

    private final RegistrationService registrations;

    public RegistrationViews(RegistrationService registrations) {
        this.registrations = registrations;
    }

    public RegistrationDetail detail(Registration r) {
        return new RegistrationDetail(
                r.id(), r.recordType(), r.status(), r.lguCode(), r.submittedAt(), r.reviewedAt(), r.reason(),
                Applicant.from(r.citizen()),
                registrations.idCardFor(r.id()).map(IdCardInfo::from).orElse(null),
                registrations.ledgerStatus(r.id()).orElse(null));
    }
}
