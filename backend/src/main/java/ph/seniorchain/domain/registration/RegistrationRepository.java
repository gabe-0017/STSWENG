package ph.seniorchain.domain.registration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Queries fetch the citizen in the same SQL statement to avoid N+1 queries. */
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    @EntityGraph(attributePaths = "citizen")
    Page<Registration> findByStatus(RegistrationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "citizen")
    Page<Registration> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "citizen")
    Optional<Registration> findWithCitizenById(UUID id);

    @EntityGraph(attributePaths = "citizen")
    Optional<Registration> findFirstByCitizenAccountIdOrderBySubmittedAtDesc(UUID accountId);
}
