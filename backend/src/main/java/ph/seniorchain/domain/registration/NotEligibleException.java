package ph.seniorchain.domain.registration;

/** The applicant does not meet the rules for the requested ID type. Maps to HTTP 400. */
public class NotEligibleException extends RuntimeException {

    public NotEligibleException(String message) {
        super(message);
    }
}
