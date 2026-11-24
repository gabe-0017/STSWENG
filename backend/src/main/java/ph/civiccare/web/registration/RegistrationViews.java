package ph.civiccare.web.registration;

import org.springframework.stereotype.Component;
import ph.civiccare.domain.registration.Registration;
import ph.civiccare.domain.registration.RegistrationService;
import ph.civiccare.web.registration.RegistrationDtos.Applicant;
import ph.civiccare.web.registration.RegistrationDtos.IdCardInfo;
import ph.civiccare.web.registration.RegistrationDtos.RegistrationDetail;

/** Builds the detail response, adding the ID card and ledger status. */
@Component
public class RegistrationViews {

    private final RegistrationService registrations;

    public RegistrationViews(RegistrationService registrations) {
        this.registrations = registrations;
    }

    /** Full view, for the registrant and LGU staff. */
    public RegistrationDetail detail(Registration r) {
        return detail(r, true);
    }

    /** includeSensitive false hides the disability classification (government admin view). */
    public RegistrationDetail detail(Registration r, boolean includeSensitive) {
        return new RegistrationDetail(
                r.id(), r.recordType(), includeSensitive ? r.disabilityClassification() : null, r.status(), r.lguCode(), r.submittedAt(), r.reviewedAt(), r.reason(),
                Applicant.from(r.citizen()),
                registrations.idCardFor(r.id()).map(IdCardInfo::from).orElse(null),
                registrations.ledgerStatus(r.id()).orElse(null));
    }
}
