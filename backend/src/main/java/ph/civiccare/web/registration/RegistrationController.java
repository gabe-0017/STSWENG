package ph.civiccare.web.registration;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ph.civiccare.application.identity.HmacService;
import ph.civiccare.domain.registration.BirthDate;
import ph.civiccare.domain.registration.GovIdNumber;
import ph.civiccare.domain.registration.NewRegistration;
import ph.civiccare.domain.registration.PersonalDetails;
import ph.civiccare.domain.registration.RecordType;
import ph.civiccare.domain.registration.RegistrationService;
import ph.civiccare.domain.registration.RegistrationStatus;
import ph.civiccare.web.common.CurrentUser;
import ph.civiccare.web.common.PageResponse;
import ph.civiccare.web.registration.RegistrationDtos.ReasonRequest;
import ph.civiccare.web.registration.RegistrationDtos.RegistrationDetail;
import ph.civiccare.web.registration.RegistrationDtos.RegistrationSummary;
import ph.civiccare.web.registration.RegistrationDtos.SubmitRegistrationRequest;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final RegistrationService registrations;
    private final HmacService hmacService;
    private final RegistrationViews views;

    public RegistrationController(RegistrationService registrations, HmacService hmacService,
                                  RegistrationViews views) {
        this.registrations = registrations;
        this.hmacService = hmacService;
        this.views = views;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SENIOR_CITIZEN')")
    RegistrationDetail submit(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubmitRegistrationRequest body) {
        PersonalDetails details = new PersonalDetails(
                body.fullName(), new BirthDate(body.birthDate()), body.address(), body.contactNumber());
        NewRegistration request = new NewRegistration(
                details, hmacService.fingerprint(GovIdNumber.of(body.govIdNumber())), RecordType.SC, body.lguCode());
        return views.detail(registrations.submit(CurrentUser.id(jwt), request));
    }

    /** Review queue, oldest first. Omit status to list everything. */
    @GetMapping
    @PreAuthorize("hasAnyRole('LGU_STAFF', 'GOV_ADMIN')")
    PageResponse<RegistrationSummary> list(
            @RequestParam Optional<RegistrationStatus> status,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.of(registrations.list(status, pageable), RegistrationSummary::from);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LGU_STAFF', 'GOV_ADMIN')")
    RegistrationDetail get(@PathVariable UUID id) {
        return views.detail(registrations.get(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('LGU_STAFF')")
    RegistrationDetail approve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return views.detail(registrations.approve(id, CurrentUser.id(jwt)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('LGU_STAFF')")
    RegistrationDetail reject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                              @Valid @RequestBody ReasonRequest body) {
        return views.detail(registrations.reject(id, CurrentUser.id(jwt), body.reason()));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAnyRole('LGU_STAFF', 'GOV_ADMIN')")
    RegistrationDetail revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                              @Valid @RequestBody ReasonRequest body) {
        return views.detail(registrations.revoke(id, CurrentUser.id(jwt), body.reason()));
    }
}
