package ph.seniorchain.domain.registration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

/** JPA mappings between value objects and plain columns. */
public final class ValueObjectConverters {

    private ValueObjectConverters() {
    }

    @Converter
    public static class BirthDateConverter implements AttributeConverter<BirthDate, LocalDate> {

        @Override
        public LocalDate convertToDatabaseColumn(BirthDate attribute) {
            return attribute == null ? null : attribute.value();
        }

        @Override
        public BirthDate convertToEntityAttribute(LocalDate column) {
            return column == null ? null : new BirthDate(column);
        }
    }

    @Converter
    public static class IdHmacConverter implements AttributeConverter<IdHmac, String> {

        @Override
        public String convertToDatabaseColumn(IdHmac attribute) {
            return attribute == null ? null : attribute.hex();
        }

        @Override
        public IdHmac convertToEntityAttribute(String column) {
            return column == null ? null : new IdHmac(column);
        }
    }
}
