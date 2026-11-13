package ph.seniorchain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Starts the app against a real Postgres: Liquibase runs and Hibernate validates every entity. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class SeniorchainApplicationTests {

	@Test
	void contextLoads() {
	}

}
