package ph.seniorchain.domain.audit;

import java.util.UUID;

/** Records who did what and when. Calls run inside the caller's transaction. */
public interface AuditTrail {

    void record(UUID actorId, AuditAction action, UUID targetId, String details);
}
