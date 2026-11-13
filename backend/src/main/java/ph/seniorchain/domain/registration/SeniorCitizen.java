package ph.seniorchain.domain.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import ph.seniorchain.domain.account.UserAccount;
import ph.seniorchain.domain.common.Require;

/** Personal data of a registrant. Lives only in Postgres, never on-chain. */
@Entity
@Table(name = "senior_citizens")
public class SeniorCitizen {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount account;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Convert(converter = ValueObjectConverters.BirthDateConverter.class)
    @Column(name = "birth_date", nullable = false)
    private BirthDate birthDate;

    @Column(nullable = false)
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Convert(converter = ValueObjectConverters.IdHmacConverter.class)
    @Column(name = "id_hmac", nullable = false, unique = true)
    private IdHmac idHmac;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    protected SeniorCitizen() {
    }

    public SeniorCitizen(UserAccount account, PersonalDetails details, IdHmac idHmac, Instant createdAt) {
        Require.notNull(details, "details");
        this.id = UUID.randomUUID();
        this.account = Require.notNull(account, "account");
        this.fullName = details.fullName();
        this.birthDate = details.birthDate();
        this.address = details.address();
        this.contactNumber = details.contactNumber();
        this.idHmac = Require.notNull(idHmac, "idHmac");
        this.createdAt = Require.notNull(createdAt, "createdAt");
    }

    public UUID id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public BirthDate birthDate() {
        return birthDate;
    }

    public String address() {
        return address;
    }

    public String contactNumber() {
        return contactNumber;
    }

    public IdHmac idHmac() {
        return idHmac;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SeniorCitizen other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SeniorCitizen[id=" + id + "]";
    }
}
