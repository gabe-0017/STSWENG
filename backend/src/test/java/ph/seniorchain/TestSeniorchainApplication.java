package ph.seniorchain;

import org.springframework.boot.SpringApplication;

/**
 * Runs the app locally with a throwaway Postgres container (needs Docker), no docker compose needed:
 * ./gradlew bootTestRun --args='--spring.profiles.active=dev'
 */
public class TestSeniorchainApplication {

	public static void main(String[] args) {
		SpringApplication.from(SeniorchainApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
