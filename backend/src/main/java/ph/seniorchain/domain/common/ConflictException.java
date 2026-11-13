package ph.seniorchain.domain.common;

/** A request that clashes with existing data or the current state. Maps to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
