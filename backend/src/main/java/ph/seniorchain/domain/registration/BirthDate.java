package ph.seniorchain.domain.registration;

import java.time.LocalDate;
import java.time.Period;
import ph.seniorchain.domain.common.Require;

/** Immutable birth date with the senior-citizen age rule (60 or older). */
public record BirthDate(LocalDate value) {

    public static final int SENIOR_AGE = 60;

    public BirthDate {
        Require.notNull(value, "birthDate");
    }

    public int ageOn(LocalDate date) {
        Require.notNull(date, "date");
        if (date.isBefore(value)) {
            throw new IllegalArgumentException("date is before the birth date");
        }
        return Period.between(value, date).getYears();
    }

    public boolean isSeniorOn(LocalDate date) {
        return !date.isBefore(value) && ageOn(date) >= SENIOR_AGE;
    }

    @Override
    public String toString() {
        return "BirthDate[hidden]";
    }
}
