package ph.civiccare.domain.registration;

import ph.civiccare.domain.common.Require;

/**
 * Everything needed to submit a registration. The raw ID number is already reduced to an HMAC.
 * A PWD registration must carry a disability classification; any other type must not.
 */
public record NewRegistration(PersonalDetails details, IdHmac idHmac, RecordType recordType,
                              DisabilityClassification disabilityClassification, String lguCode) {

    public NewRegistration {
        Require.notNull(details, "details");
        Require.notNull(idHmac, "idHmac");
        Require.notNull(recordType, "recordType");
        Require.notBlank(lguCode, "lguCode");
        if (recordType == RecordType.PWD && disabilityClassification == null) {
            throw new IllegalArgumentException("disabilityClassification is required for a PWD registration");
        }
        if (recordType != RecordType.PWD && disabilityClassification != null) {
            throw new IllegalArgumentException("disabilityClassification only applies to a PWD registration");
        }
    }
}
