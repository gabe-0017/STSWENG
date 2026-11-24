package ph.civiccare.domain.registration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitizenRepository extends JpaRepository<Citizen, UUID> {

    boolean existsByIdHmac(IdHmac idHmac);

    Optional<Citizen> findByAccountId(UUID accountId);
}
