package ph.seniorchain.domain.ledger;

import java.util.Optional;
import java.util.UUID;
import ph.seniorchain.domain.registration.IdCard;
import ph.seniorchain.domain.registration.Registration;

/**
 * The tamper-evident ledger of approved records. The domain does not know it is a blockchain:
 * implementations can queue writes to a smart contract (Option B) or a hash-chained table (Option A).
 * Calls run inside the caller's transaction.
 */
public interface LedgerService {

    void anchorApproval(Registration registration, IdCard card);

    void anchorRevocation(Registration registration);

    /** Sync state of the most recent ledger write for this registration, if any. */
    Optional<LedgerSyncStatus> syncStatus(UUID registrationId);
}
