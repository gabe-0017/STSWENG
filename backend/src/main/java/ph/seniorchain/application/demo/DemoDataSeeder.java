package ph.seniorchain.application.demo;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ph.seniorchain.application.auth.AuthService;
import ph.seniorchain.application.identity.HmacService;
import ph.seniorchain.config.SeniorChainProperties;
import ph.seniorchain.domain.account.Role;
import ph.seniorchain.domain.account.UserAccount;
import ph.seniorchain.domain.account.UserAccountRepository;
import ph.seniorchain.domain.registration.BirthDate;
import ph.seniorchain.domain.registration.GovIdNumber;
import ph.seniorchain.domain.registration.NewRegistration;
import ph.seniorchain.domain.registration.PersonalDetails;
import ph.seniorchain.domain.registration.RecordType;
import ph.seniorchain.domain.registration.RegistrationService;

/**
 * Seeds SYNTHETIC demo accounts so the app can be presented right after deploy:
 * one account per role, plus a few pending registrations in the LGU queue.
 * Runs only when seniorchain.demo-data.enabled=true and a password is set. Safe to run repeatedly.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    public static final String LGU_EMAIL = "lgu@seniorchain.test";
    public static final String ESTABLISHMENT_EMAIL = "establishment@seniorchain.test";
    public static final String ADMIN_EMAIL = "admin@seniorchain.test";
    public static final String SENIOR_EMAIL = "senior@seniorchain.test";
    private static final String DEMO_LGU_CODE = "QC-OSCA";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SeniorChainProperties properties;
    private final UserAccountRepository accounts;
    private final AuthService authService;
    private final RegistrationService registrationService;
    private final HmacService hmacService;

    public DemoDataSeeder(SeniorChainProperties properties, UserAccountRepository accounts, AuthService authService,
                          RegistrationService registrationService, HmacService hmacService) {
        this.properties = properties;
        this.accounts = accounts;
        this.authService = authService;
        this.registrationService = registrationService;
        this.hmacService = hmacService;
    }

    @Override
    public void run(ApplicationArguments args) {
        SeniorChainProperties.DemoData demo = properties.demoData();
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
        authService.createAccount(LGU_EMAIL, password, "Demo LGU Staff (QC OSCA)", Role.LGU_STAFF);
        authService.createAccount(ESTABLISHMENT_EMAIL, password, "Demo Botika Pharmacy", Role.ESTABLISHMENT);
        authService.createAccount(ADMIN_EMAIL, password, "Demo NCSC Admin", Role.GOV_ADMIN);
        // No registration yet, so it can be used to register live during a demo
        authService.createAccount(SENIOR_EMAIL, password, "Demo Senior Citizen", Role.SENIOR_CITIZEN);

        seedPendingRegistration("sample1@seniorchain.test", password, "Lorenzo Sample Reyes",
                LocalDate.of(1955, 3, 14), "12 Sampaguita St, Quezon City", "DEMO-0000-0001");
        seedPendingRegistration("sample2@seniorchain.test", password, "Teresita Sample Cruz",
                LocalDate.of(1948, 11, 2), "45 Narra Ave, Quezon City", "DEMO-0000-0002");
        seedPendingRegistration("sample3@seniorchain.test", password, "Ramon Sample Santos",
                LocalDate.of(1960, 7, 21), "8 Mabini Rd, Quezon City", "DEMO-0000-0003");

        log.info("Seeded demo accounts ({}, {}, {}, {}) and 3 pending registrations",
                LGU_EMAIL, ESTABLISHMENT_EMAIL, ADMIN_EMAIL, SENIOR_EMAIL);
    }

    private void seedPendingRegistration(String email, String password, String name, LocalDate birthDate,
                                         String address, String govId) {
        UserAccount account = authService.createAccount(email, password, name, Role.SENIOR_CITIZEN);
        PersonalDetails details = new PersonalDetails(name, new BirthDate(birthDate), address, null);
        registrationService.submit(account.id(), new NewRegistration(
                details, hmacService.fingerprint(GovIdNumber.of(govId)), RecordType.SC, DEMO_LGU_CODE));
    }
}
