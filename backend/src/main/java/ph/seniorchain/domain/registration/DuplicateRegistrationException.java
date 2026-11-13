package ph.seniorchain.domain.registration;

import ph.seniorchain.domain.common.ConflictException;

/** The message is deliberately vague so it does not confirm whose ID number is registered. */
public class DuplicateRegistrationException extends ConflictException {

    public DuplicateRegistrationException() {
        super("A registration already exists for these details");
    }
}
