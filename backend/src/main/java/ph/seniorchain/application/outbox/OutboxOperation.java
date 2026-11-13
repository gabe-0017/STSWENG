package ph.seniorchain.application.outbox;

/** Matches the contract functions: register(...) and revoke(...). */
public enum OutboxOperation {
    REGISTER,
    REVOKE
}
