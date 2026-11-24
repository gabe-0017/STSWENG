package ph.civiccare.domain.registration;

import java.util.Collection;
import java.util.List;
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

    @EntityGraph(attributePaths = "citizen")
    List<Registration> findByCitizenAccountIdOrderBySubmittedAtDesc(UUID accountId);

    boolean existsByCitizenIdAndRecordTypeAndStatusIn(UUID citizenId, RecordType recordType,
                                                      Collection<RegistrationStatus> statuses);
}
