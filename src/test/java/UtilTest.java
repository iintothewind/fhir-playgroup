import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
public class UtilTest {


    @Test
    public void testReadLines01() {
        final List<String> lines = Util.readLines("names.txt");
        Assertions.assertThat(lines).isNotEmpty();
        Assertions.assertThat(lines).hasSize(20);
    }

    @Test
    public void testReadLines02() {
        final List<String> lines = Util.readLines("names1.txt");
        Assertions.assertThat(lines).isEmpty();
    }


    @Test
    public void testReadLines03() {
        final List<String> lines = Util.readLines(null);
        Assertions.assertThat(lines).isEmpty();
    }

    public static Patient mkPatient(final String fistName, final String lastName, final String dob) {
        Patient p = new Patient();
        final HumanName name = new HumanName();
        if (Objects.nonNull(fistName)) {
            name.setGiven(ImmutableList.of(new StringType(fistName)));
        }
        if (Objects.nonNull(lastName)) {
            name.setFamily(lastName);
        }
        if (Objects.nonNull(fistName) || Objects.nonNull(lastName)) {
            p.setName(ImmutableList.of(name));
        }
        final Date dateOfBirth = Try.of(() -> Util.localDateToDate(LocalDate.parse(dob, Util.DATE_FORMAT))).getOrNull();
        if (Objects.nonNull(dateOfBirth)) {
            p.setBirthDate(dateOfBirth);
        }
        return p;
    }

    @Test
    public void testExtractFirstName01() {
        final Patient patient = mkPatient("John", "Doe", "2024-01-01");
        final String firstName = Util.extractFirstName(patient);
        Assertions.assertThat(firstName).isEqualTo("John");
    }

    @Test
    public void testExtractFirstName02() {
        final Patient patient = mkPatient(null, "Doe", "2024-01-01");
        final String firstName = Util.extractFirstName(patient);
        Assertions.assertThat(firstName).isEmpty();
    }

    @Test
    public void testExtractFirstName03() {
        final Patient patient = null;
        final String firstName = Util.extractFirstName(patient);
        Assertions.assertThat(firstName).isEmpty();
    }

    @Test
    public void testExtractLastName01() {
        final Patient patient = mkPatient("John", "Doe", "2024-01-01");
        final String lastName = Util.extractLastName(patient);
        Assertions.assertThat(lastName).isEqualTo("Doe");
    }


    @Test
    public void testExtractLastName02() {
        final Patient patient = mkPatient("John", null, "2024-01-01");
        final String lastName = Util.extractLastName(patient);
        Assertions.assertThat(lastName).isEmpty();
    }

    @Test
    public void testExtractLastName03() {
        final Patient patient = null;
        final String lastName = Util.extractLastName(patient);
        Assertions.assertThat(lastName).isEmpty();
    }


    @Test
    public void testExtractDob01() {
        final Patient patient = mkPatient("John", "Doe", "2024-01-01");
        final String dob = Util.extractDob(patient);
        Assertions.assertThat(dob).isEqualTo("2024-01-01");
    }

    @Test
    public void testExtractDob02() {
        final Patient patient = mkPatient("John", "Doe", null);
        final String dob = Util.extractDob(patient);
        Assertions.assertThat(dob).isEmpty();
    }

    @Test
    public void testExtractDob03() {
        final Patient patient = null;
        final String dob = Util.extractDob(patient);
        Assertions.assertThat(dob).isEmpty();
    }
}
