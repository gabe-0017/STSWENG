package ph.seniorchain.domain.registration;

import ph.seniorchain.domain.common.Require;

/** The personal fields a registrant submits. contactNumber is optional. */
public record PersonalDetails(String fullName, BirthDate birthDate, String address, String contactNumber) {

    public PersonalDetails {
        fullName = Require.maxLength(Require.notBlank(fullName, "fullName"), 200, "fullName");
        Require.notNull(birthDate, "birthDate");
        address = Require.maxLength(Require.notBlank(address, "address"), 500, "address");
        contactNumber = contactNumber == null || contactNumber.isBlank()
                ? null
                : Require.maxLength(contactNumber.strip(), 30, "contactNumber");
    }

    @Override
    public String toString() {
        return "PersonalDetails[hidden]";
    }
}
