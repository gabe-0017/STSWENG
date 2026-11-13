package ph.seniorchain.domain.registration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdCardRepository extends JpaRepository<IdCard, UUID> {

    Optional<IdCard> findByRegistrationId(UUID registrationId);
}
