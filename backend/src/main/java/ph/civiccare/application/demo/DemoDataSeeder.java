package ph.civiccare.application.demo;

import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ph.civiccare.application.auth.AuthService;
import ph.civiccare.application.identity.HmacService;
import ph.civiccare.config.CivicCareProperties;
import ph.civiccare.domain.account.Role;
import ph.civiccare.domain.account.UserAccount;
import ph.civiccare.domain.account.UserAccountRepository;
import ph.civiccare.domain.registration.BirthDate;
import ph.civiccare.domain.registration.DisabilityClassification;
import ph.civiccare.domain.registration.GovIdNumber;
import ph.civiccare.domain.registration.NewRegistration;
import ph.civiccare.domain.registration.PersonalDetails;
import ph.civiccare.domain.registration.RecordType;
import ph.civiccare.domain.registration.Registration;
import ph.civiccare.domain.registration.RegistrationService;

/**
 * Seeds SYNTHETIC demo accounts so the app can be presented right after deploy:
 * one account per staff role, plus four citizens (an approved and a pending one for each of SC and PWD).
 * Runs only when civiccare.demo-data.enabled=true and a password is set. Safe to run repeatedly.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    public static final String LGU_EMAIL = "lgu@civiccare.test";
    public static final String ESTABLISHMENT_EMAIL = "establishment@civiccare.test";
    public static final String ADMIN_EMAIL = "admin@civiccare.test";
    private static final String DEMO_LGU_CODE = "QC-OSCA";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final CivicCareProperties properties;
    private final UserAccountRepository accounts;
    private final AuthService authService;
    private final RegistrationService registrationService;
    private final HmacService hmacService;

    public DemoDataSeeder(CivicCareProperties properties, UserAccountRepository accounts, AuthService authService,
                          RegistrationService registrationService, HmacService hmacService) {
        this.properties = properties;
        this.accounts = accounts;
        this.authService = authService;
        this.registrationService = registrationService;
        this.hmacService = hmacService;
    }

    @Override
    public void run(ApplicationArguments args) {
        CivicCareProperties.DemoData demo = properties.demoData();
        if (!demo.enabled()) {
            return;
        }
        if (demo.password() == null || demo.password().isBlank()) {
            log.warn("Demo data is enabled but DEMO_PASSWORD is not set; skipping demo accounts");
            return;
        }
        if (accounts.existsByEmail(LGU_EMAIL)) {
            log.info("Demo accounts already exist");
            return;
        }

        String password = demo.password();
        UserAccount lgu = authService.createAccount(LGU_EMAIL, password, "Demo LGU Staff (QC OSCA)", Role.LGU_STAFF);
        authService.createAccount(ESTABLISHMENT_EMAIL, password, "Demo Botika Pharmacy", Role.ESTABLISHMENT);
        authService.createAccount(ADMIN_EMAIL, password, "Demo NCSC Admin", Role.GOV_ADMIN);

        // Four citizens: one approved and one pending for each ID type
        seedRegistration("approved-senior@civiccare.test", password, "Lorenzo Sample Reyes",
                LocalDate.of(1955, 3, 14), "12 Sampaguita St, Quezon City", "DEMO-0000-0001",
                RecordType.SC, null, lgu.id());
        seedRegistration("approved-pwd@civiccare.test", password, "Andrea Sample Dizon",
                LocalDate.of(1992, 9, 5), "3 Rizal St, Quezon City", "DEMO-0000-0002",
                RecordType.PWD, DisabilityClassification.VISUAL, lgu.id());
        seedRegistration("pending-senior@civiccare.test", password, "Teresita Sample Cruz",
                LocalDate.of(1948, 11, 2), "45 Narra Ave, Quezon City", "DEMO-0000-0003",
                RecordType.SC, null, null);
        seedRegistration("pending-pwd@civiccare.test", password, "Ramon Sample Santos",
                LocalDate.of(1988, 7, 21), "8 Mabini Rd, Quezon City", "DEMO-0000-0004",
                RecordType.PWD, DisabilityClassification.PHYSICAL, null);

        log.info("Seeded demo accounts ({}, {}, {}) and 4 citizens (2 approved, 2 pending)",
                LGU_EMAIL, ESTABLISHMENT_EMAIL, ADMIN_EMAIL);
    }

    /** Creates a citizen account and submits a registration; approves it too if reviewerId is given. */
    private void seedRegistration(String email, String password, String name, LocalDate birthDate,
                                  String address, String govId, RecordType type,
                                  DisabilityClassification classification, UUID reviewerId) {
        UserAccount account = authService.createAccount(email, password, name, Role.CITIZEN);
        PersonalDetails details = new PersonalDetails(name, new BirthDate(birthDate), address, null);
        Registration registration = registrationService.submit(account.id(), new NewRegistration(
                details, hmacService.fingerprint(GovIdNumber.of(govId)), type, classification, DEMO_LGU_CODE));
        if (reviewerId != null) {
            registrationService.approve(registration.id(), reviewerId);
        }
    }
}
