package ph.civiccare.domain.registration;

import ph.civiccare.domain.common.ConflictException;

public class IllegalStatusTransitionException extends ConflictException {

    public IllegalStatusTransitionException(RegistrationStatus from, RegistrationStatus to) {
        super("Cannot change a registration from " + from + " to " + to);
    }
}
