package ph.seniorchain.domain.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import ph.seniorchain.domain.common.Require;

/** The ID issued when a registration is approved. */
@Entity
@Table(name = "id_cards")
public class IdCard {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Version
    private Long version;

    protected IdCard() {
    }

    private IdCard(Registration registration, String cardNumber, Instant issuedAt, LocalDate validUntil) {
        this.id = UUID.randomUUID();
        this.registration = registration;
        this.cardNumber = Require.notBlank(cardNumber, "cardNumber");
        this.issuedAt = Require.notNull(issuedAt, "issuedAt");
        this.validUntil = Require.notNull(validUntil, "validUntil");
    }

    /** Only an approved (active) registration can get a card. */
    public static IdCard issue(Registration registration, String cardNumber, Instant issuedAt, LocalDate validUntil) {
        Require.notNull(registration, "registration");
        if (!registration.isActive()) {
            throw new IllegalStateException("Cannot issue an ID for a registration that is " + registration.status());
        }
        return new IdCard(registration, cardNumber, issuedAt, validUntil);
    }

    public UUID id() {
        return id;
    }

    public String cardNumber() {
        return cardNumber;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public LocalDate validUntil() {
        return validUntil;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof IdCard other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "IdCard[id=" + id + ", cardNumber=" + cardNumber + ", validUntil=" + validUntil + "]";
    }
}
