package ph.seniorchain.application.outbox;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ph.seniorchain.domain.ledger.LedgerService;
import ph.seniorchain.domain.ledger.LedgerSyncStatus;
import ph.seniorchain.domain.registration.IdCard;
import ph.seniorchain.domain.registration.Registration;

/**
 * Queues ledger writes in chain_outbox instead of calling the blockchain directly,
 * so requests never wait on it. The payload holds only the HMAC, type and expiry: no personal data.
 */
@Service
public class OutboxLedgerService implements LedgerService {

    private final ChainOutboxRepository outbox;
    private final Clock clock;

    public OutboxLedgerService(ChainOutboxRepository outbox, Clock clock) {
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    public void anchorApproval(Registration registration, IdCard card) {
        String payload = "{\"idHmac\":\"%s\",\"recordType\":\"%s\",\"validUntil\":\"%s\"}".formatted(
                registration.citizen().idHmac().hex(), registration.recordType(), card.validUntil());
        outbox.save(new ChainOutboxEntry(registration.id(), OutboxOperation.REGISTER, payload, clock.instant()));
    }

    @Override
    public void anchorRevocation(Registration registration) {
        String payload = "{\"idHmac\":\"%s\"}".formatted(registration.citizen().idHmac().hex());
        outbox.save(new ChainOutboxEntry(registration.id(), OutboxOperation.REVOKE, payload, clock.instant()));
    }

    @Override
    public Optional<LedgerSyncStatus> syncStatus(UUID registrationId) {
        return outbox.findFirstByRegistrationIdOrderByCreatedAtDesc(registrationId).map(ChainOutboxEntry::status);
    }
}
