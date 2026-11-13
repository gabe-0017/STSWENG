package ph.seniorchain.web.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ph.seniorchain.application.auth.AuthService;
import ph.seniorchain.web.auth.AuthDtos.AccountResponse;
import ph.seniorchain.web.auth.AuthDtos.LoginRequest;
import ph.seniorchain.web.auth.AuthDtos.SignupRequest;
import ph.seniorchain.web.auth.AuthDtos.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authService.login(request.email(), request.password()));
    }

    /** Senior citizen self sign-up. Log in afterwards to get a token. */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse signup(@Valid @RequestBody SignupRequest request) {
        return AccountResponse.from(
                authService.signUpSeniorCitizen(request.email(), request.password(), request.fullName()));
    }
}
