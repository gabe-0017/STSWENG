package ph.seniorchain.application.outbox;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChainOutboxRepository extends JpaRepository<ChainOutboxEntry, UUID> {

    Optional<ChainOutboxEntry> findFirstByRegistrationIdOrderByCreatedAtDesc(UUID registrationId);

    List<ChainOutboxEntry> findByRegistrationIdOrderByCreatedAtAsc(UUID registrationId);
}
