package ph.seniorchain.domain.registration;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeniorCitizenRepository extends JpaRepository<SeniorCitizen, UUID> {

    boolean existsByIdHmac(IdHmac idHmac);

    boolean existsByAccountId(UUID accountId);
}
