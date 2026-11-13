package ph.seniorchain.domain.ledger;

/** PENDING shows in the UI as "Approved – pending on-chain". */
public enum LedgerSyncStatus {
    PENDING,
    CONFIRMED,
    FAILED
}
