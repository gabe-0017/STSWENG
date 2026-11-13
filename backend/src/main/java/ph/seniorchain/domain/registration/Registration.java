package ph.seniorchain.domain.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import ph.seniorchain.domain.common.Require;

/**
 * The registration workflow for one ID. State changes only go through the business methods;
 * illegal transitions throw {@link IllegalStatusTransitionException}.
 * The version column (optimistic locking) stops two staff members changing it at the same time.
 */
@Entity
@Table(name = "registrations")
public class Registration {

    private static final int MAX_LGU_CODE = 20;
    private static final int MAX_REASON = 500;

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private SeniorCitizen citizen;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "lgu_code", nullable = false)
    private String lguCode;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column
    private String reason;

    @Version
    private Long version;

    protected Registration() {
    }

    private Registration(SeniorCitizen citizen, RecordType recordType, String lguCode, Instant submittedAt) {
        this.id = UUID.randomUUID();
        this.citizen = Require.notNull(citizen, "citizen");
        this.recordType = Require.notNull(recordType, "recordType");
        this.lguCode = Require.maxLength(Require.notBlank(lguCode, "lguCode"), MAX_LGU_CODE, "lguCode");
        this.submittedAt = Require.notNull(submittedAt, "submittedAt");
        this.status = RegistrationStatus.SUBMITTED;
    }

    public static Registration submit(SeniorCitizen citizen, RecordType recordType, String lguCode, Instant now) {
        return new Registration(citizen, recordType, lguCode, now);
    }

    public void approve(UUID reviewerId, Instant now) {
        requireStatus(RegistrationStatus.APPROVED, RegistrationStatus.SUBMITTED);
        markReviewed(RegistrationStatus.APPROVED, reviewerId, null, now);
    }

    public void reject(UUID reviewerId, String reason, Instant now) {
        requireStatus(RegistrationStatus.REJECTED, RegistrationStatus.SUBMITTED);
        markReviewed(RegistrationStatus.REJECTED, reviewerId, requireReason(reason), now);
    }

    public void revoke(UUID actorId, String reason, Instant now) {
        if (!status.isActive()) {
            throw new IllegalStatusTransitionException(status, RegistrationStatus.REVOKED);
        }
        markReviewed(RegistrationStatus.REVOKED, actorId, requireReason(reason), now);
    }

    public boolean isActive() {
        return status.isActive();
    }

    private void requireStatus(RegistrationStatus target, RegistrationStatus required) {
        if (status != required) {
            throw new IllegalStatusTransitionException(status, target);
        }
    }

    private static String requireReason(String reason) {
        return Require.maxLength(Require.notBlank(reason, "reason"), MAX_REASON, "reason");
    }

    private void markReviewed(RegistrationStatus newStatus, UUID actorId, String newReason, Instant now) {
        this.reviewedBy = Require.notNull(actorId, "actorId");
        this.reviewedAt = Require.notNull(now, "now");
        this.reason = newReason;
        this.status = newStatus;
    }

    public UUID id() {
        return id;
    }

    public SeniorCitizen citizen() {
        return citizen;
    }

    public RecordType recordType() {
        return recordType;
    }

    public RegistrationStatus status() {
        return status;
    }

    public String lguCode() {
        return lguCode;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public Instant reviewedAt() {
        return reviewedAt;
    }

    public String reason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Registration other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Registration[id=" + id + ", type=" + recordType + ", status=" + status + "]";
    }
}
