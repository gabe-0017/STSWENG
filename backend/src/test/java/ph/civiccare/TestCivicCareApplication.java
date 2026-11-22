package ph.civiccare;

import org.springframework.boot.SpringApplication;

/**
 * Runs the app locally with a throwaway Postgres container (needs Docker), no docker compose needed:
 * ./gradlew bootTestRun --args='--spring.profiles.active=dev'
 */
public class TestCivicCareApplication {

	public static void main(String[] args) {
		SpringApplication.from(CivicCareApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
