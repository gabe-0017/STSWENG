package ph.seniorchain.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ph.seniorchain.TestcontainersConfiguration;
import ph.seniorchain.application.audit.AuditLogRepository;
import ph.seniorchain.application.auth.AuthService;
import ph.seniorchain.application.outbox.ChainOutboxEntry;
import ph.seniorchain.application.outbox.ChainOutboxRepository;
import ph.seniorchain.application.outbox.OutboxOperation;
import ph.seniorchain.domain.account.Role;

/**
 * End-to-end through HTTP, security, Postgres and Liquibase:
 * sign up -> submit -> duplicate rejected -> LGU approves -> ledger write queued -> revoke.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RegistrationFlowIntegrationTest {

    private static final String PASSWORD = "Test-password-1";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private ChainOutboxRepository outbox;
    @Autowired
    private AuditLogRepository auditLog;

    private String lguToken;

    @BeforeEach
    void createLguStaff() throws Exception {
        String email = "lgu-" + UUID.randomUUID() + "@seniorchain.test";
        authService.createAccount(email, PASSWORD, "Test LGU Staff", Role.LGU_STAFF);
        lguToken = login(email);
    }

    @Test
    void fullRegistrationLifecycle() throws Exception {
        String govId = randomGovId();
        String seniorToken = signUpAndLogin();

        mvc.perform(withToken(get("/api/v1/me/registration"), seniorToken))
                .andExpect(status().isNotFound());

        String submitted = mvc.perform(withToken(post("/api/v1/registrations"), seniorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson("1955-03-14", govId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.idCard").value(nullValue()))
                .andExpect(jsonPath("$.ledgerStatus").value(nullValue()))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(submitted, "$.id");

        // Same ID number typed differently from another account is still a duplicate
        mvc.perform(withToken(post("/api/v1/registrations"), signUpAndLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson("1955-03-14", govId.toLowerCase().replace("-", " "))))
                .andExpect(status().isConflict());

        mvc.perform(withToken(get("/api/v1/registrations").param("status", "SUBMITTED").param("size", "100"),
                        lguToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(id)));

        mvc.perform(withToken(post("/api/v1/registrations/" + id + "/approve"), lguToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.idCard.cardNumber").value(matchesPattern("SC-\\d{4}-\\d{8}")))
                .andExpect(jsonPath("$.ledgerStatus").value("PENDING"));

        mvc.perform(withToken(post("/api/v1/registrations/" + id + "/approve"), lguToken))
                .andExpect(status().isConflict());

        mvc.perform(withToken(get("/api/v1/me/registration"), seniorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(withToken(post("/api/v1/registrations/" + id + "/revoke"), lguToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Fraudulent documents\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        UUID registrationId = UUID.fromString(id);
        assertThat(outbox.findByRegistrationIdOrderByCreatedAtAsc(registrationId))
                .extracting(ChainOutboxEntry::operation)
                .containsExactly(OutboxOperation.REGISTER, OutboxOperation.REVOKE);
        assertThat(outbox.findByRegistrationIdOrderByCreatedAtAsc(registrationId).getFirst().payload())
                .doesNotContain("Sample", "Test St");
        assertThat(auditLog.findByTargetIdOrderByCreatedAtAsc(registrationId)).hasSize(3);
    }

    @Test
    void rejectRequiresReason() throws Exception {
        String id = submitRegistration();

        mvc.perform(withToken(post("/api/v1/registrations/" + id + "/reject"), lguToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.reason").exists());
    }

    @Test
    void applicantUnder60IsRejected() throws Exception {
        mvc.perform(withToken(post("/api/v1/registrations"), signUpAndLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson("1990-01-01", randomGovId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Not eligible"));
    }

    @Test
    void seniorCitizenCannotSeeReviewQueue() throws Exception {
        mvc.perform(withToken(get("/api/v1/registrations"), signUpAndLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void seniorCitizenCannotApprove() throws Exception {
        String id = submitRegistration();

        mvc.perform(withToken(post("/api/v1/registrations/" + id + "/approve"), signUpAndLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestsWithoutTokenAreUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/registrations")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@seniorchain.test\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private String submitRegistration() throws Exception {
        String body = mvc.perform(withToken(post("/api/v1/registrations"), signUpAndLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson("1950-06-01", randomGovId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String signUpAndLogin() throws Exception {
        String email = "senior-" + UUID.randomUUID() + "@seniorchain.test";
        mvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"Test Sample\"}"
                                .formatted(email, PASSWORD)))
                .andExpect(status().isCreated());
        return login(email);
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private static String registrationJson(String birthDate, String govId) {
        return """
                {"fullName":"Test Sample Person","birthDate":"%s","address":"1 Test St, Quezon City",
                 "contactNumber":"09170000000","govIdNumber":"%s","lguCode":"QC-OSCA"}
                """.formatted(birthDate, govId);
    }

    /** Synthetic ID number, unique per test so tests do not collide in the shared database. */
    private static String randomGovId() {
        return "TEST-%012d".formatted(ThreadLocalRandom.current().nextLong(1_000_000_000_000L));
    }
}
