package ph.civiccare.web.me;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.civiccare.application.auth.AuthService;
import ph.civiccare.domain.common.NotFoundException;
import ph.civiccare.domain.registration.RegistrationService;
import ph.civiccare.web.auth.AuthDtos.AccountResponse;
import ph.civiccare.web.common.CurrentUser;
import ph.civiccare.web.registration.RegistrationDtos.RegistrationDetail;
import ph.civiccare.web.registration.RegistrationViews;

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

    /** The citizen's most recent registration and its status. 404 if they have not registered yet. */
    @GetMapping("/registration")
    @PreAuthorize("hasRole('CITIZEN')")
    RegistrationDetail myRegistration(@AuthenticationPrincipal Jwt jwt) {
        return registrations.findLatestForAccount(CurrentUser.id(jwt))
                .map(views::detail)
                .orElseThrow(() -> new NotFoundException("No registration yet"));
    }

    /** All of the citizen's registrations (Senior Citizen and/or PWD), newest first. Empty if none. */
    @GetMapping("/registrations")
    @PreAuthorize("hasRole('CITIZEN')")
    List<RegistrationDetail> myRegistrations(@AuthenticationPrincipal Jwt jwt) {
        return registrations.findAllForAccount(CurrentUser.id(jwt)).stream().map(views::detail).toList();
    }
}
