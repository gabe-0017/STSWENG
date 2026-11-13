package ph.seniorchain.domain.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValueObjectsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    @Test
    void seniorOnSixtiethBirthday() {
        assertThat(new BirthDate(LocalDate.of(1966, 11, 10)).isSeniorOn(TODAY)).isTrue();
    }

    @Test
    void notSeniorTheDayBefore() {
        assertThat(new BirthDate(LocalDate.of(1966, 11, 11)).isSeniorOn(TODAY)).isFalse();
    }

    @Test
    void notSeniorIfBornInTheFuture() {
        assertThat(new BirthDate(LocalDate.of(2030, 1, 1)).isSeniorOn(TODAY)).isFalse();
    }

    @Test
    void govIdNumberIgnoresSpacesDashesAndCase() {
        assertThat(GovIdNumber.of(" ab-12 34-cd ")).isEqualTo(GovIdNumber.of("AB1234CD"));
    }

    @Test
    void govIdNumberRejectsSymbols() {
        assertThatThrownBy(() -> GovIdNumber.of("12$45")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void govIdNumberHidesValueInToString() {
        assertThat(GovIdNumber.of("AB1234").toString()).doesNotContain("AB1234");
    }

    @Test
    void idHmacRequires64HexChars() {
        assertThatThrownBy(() -> new IdHmac("abc")).isInstanceOf(IllegalArgumentException.class);
    }
}
