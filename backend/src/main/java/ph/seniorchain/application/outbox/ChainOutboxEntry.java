package ph.seniorchain.application.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import ph.seniorchain.domain.common.Require;
import ph.seniorchain.domain.ledger.LedgerSyncStatus;

/**
 * One pending blockchain write. Saved in the same transaction as the state change;
 * a scheduled processor (Sprint 3) sends it to the contract and marks it CONFIRMED.
 */
@Entity
@Table(name = "chain_outbox")
public class ChainOutboxEntry {

    @Id
    private UUID id;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxOperation operation;

    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerSyncStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Version
    private Long version;

    protected ChainOutboxEntry() {
    }

    public ChainOutboxEntry(UUID registrationId, OutboxOperation operation, String payload, Instant now) {
        this.id = UUID.randomUUID();
        this.registrationId = Require.notNull(registrationId, "registrationId");
        this.operation = Require.notNull(operation, "operation");
        this.payload = Require.notBlank(payload, "payload");
        this.createdAt = Require.notNull(now, "now");
        this.nextAttemptAt = now;
        this.status = LedgerSyncStatus.PENDING;
    }

    public UUID id() {
        return id;
    }

    public UUID registrationId() {
        return registrationId;
    }

    public OutboxOperation operation() {
        return operation;
    }

    public String payload() {
        return payload;
    }

    public LedgerSyncStatus status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChainOutboxEntry other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ChainOutboxEntry[id=" + id + ", op=" + operation + ", status=" + status
                + ", attempts=" + attempts + "]";
    }
}
