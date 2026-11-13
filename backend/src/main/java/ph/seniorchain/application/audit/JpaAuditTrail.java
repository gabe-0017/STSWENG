package ph.seniorchain.application.audit;

import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ph.seniorchain.domain.audit.AuditAction;
import ph.seniorchain.domain.audit.AuditTrail;

@Service
public class JpaAuditTrail implements AuditTrail {

    private final AuditLogRepository repository;
    private final Clock clock;

    public JpaAuditTrail(AuditLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void record(UUID actorId, AuditAction action, UUID targetId, String details) {
        repository.save(new AuditLogEntry(actorId, action, targetId, details, clock.instant()));
    }
}
