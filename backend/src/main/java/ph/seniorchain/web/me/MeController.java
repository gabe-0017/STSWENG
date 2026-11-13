package ph.seniorchain.web.me;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.seniorchain.application.auth.AuthService;
import ph.seniorchain.domain.common.NotFoundException;
import ph.seniorchain.domain.registration.RegistrationService;
import ph.seniorchain.web.auth.AuthDtos.AccountResponse;
import ph.seniorchain.web.common.CurrentUser;
import ph.seniorchain.web.registration.RegistrationDtos.RegistrationDetail;
import ph.seniorchain.web.registration.RegistrationViews;

/** Endpoints about the logged-in user. */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthService authService;
    private final RegistrationService registrations;
    private final RegistrationViews views;

    public MeController(AuthService authService, RegistrationService registrations, RegistrationViews views) {
        this.authService = authService;
        this.registrations = registrations;
        this.views = views;
    }

    @GetMapping
    AccountResponse me(@AuthenticationPrincipal Jwt jwt) {
        return AccountResponse.from(authService.get(CurrentUser.id(jwt)));
    }

    /** The senior citizen's own registration and its status. 404 if they have not registered yet. */
    @GetMapping("/registration")
    @PreAuthorize("hasRole('SENIOR_CITIZEN')")
    RegistrationDetail myRegistration(@AuthenticationPrincipal Jwt jwt) {
        return registrations.findLatestForAccount(CurrentUser.id(jwt))
                .map(views::detail)
                .orElseThrow(() -> new NotFoundException("No registration yet"));
    }
}
