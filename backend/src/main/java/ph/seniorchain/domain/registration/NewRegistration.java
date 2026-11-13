package ph.seniorchain.domain.registration;

import ph.seniorchain.domain.common.Require;

/** Everything needed to submit a registration. The raw ID number is already reduced to an HMAC. */
public record NewRegistration(PersonalDetails details, IdHmac idHmac, RecordType recordType, String lguCode) {

    public NewRegistration {
        Require.notNull(details, "details");
        Require.notNull(idHmac, "idHmac");
        Require.notNull(recordType, "recordType");
        Require.notBlank(lguCode, "lguCode");
    }
}
