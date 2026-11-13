package ph.seniorchain.application.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import ph.seniorchain.domain.audit.AuditAction;
import ph.seniorchain.domain.common.Require;

/** Append-only audit record. */
@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    protected AuditLogEntry() {
    }

    public AuditLogEntry(UUID actorId, AuditAction action, UUID targetId, String details, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.actorId = Require.notNull(actorId, "actorId");
        this.action = Require.notNull(action, "action");
        this.targetId = Require.notNull(targetId, "targetId");
        this.details = details;
        this.createdAt = Require.notNull(createdAt, "createdAt");
    }

    public UUID actorId() {
        return actorId;
    }

    public AuditAction action() {
        return action;
    }

    public UUID targetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AuditLogEntry other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AuditLogEntry[id=" + id + ", action=" + action + ", target=" + targetId + "]";
    }
}
