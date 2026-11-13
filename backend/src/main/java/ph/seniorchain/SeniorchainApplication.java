package ph.seniorchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SeniorchainApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeniorchainApplication.class, args);
	}

}
