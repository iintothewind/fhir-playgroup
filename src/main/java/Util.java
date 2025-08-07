import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Util {
    public final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * given a Patient p, extract the first given name of its first name element, in other words, to extract p.name[0].given[0]
     * return empty string if first given name is not existing.
     * <code>
     * {
     * "fullUrl": "https://hapi.fhir.org/baseR4/Patient/pat2",
     * "resource": {
     * "resourceType": "Patient",
     * "id": "pat2",
     * "meta": {
     * "versionId": "6",
     * "lastUpdated": "2025-02-03T10:47:21.221+00:00",
     * "source": "#n1HxCzy7fO78KKI0",
     * "security": [
     * {
     * "system": "http://essai.abcfg.com/",
     * "code": "code1",
     * "display": "coded1"
     * }
     * ],
     * "tag": [
     * {
     * "system": "http://essai.abcfg.com/",
     * "code": "tag1",
     * "display": "tagd1"
     * }
     * ]
     * },
     * "text": {
     * "status": "generated",
     * "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">John <b>SMITH </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Date of birth</td><td><span>15 March 1985</span></td></tr></tbody></table></div>"
     * },
     * "name": [
     * {
     * "use": "official",
     * "family": "Smith",
     * "given": [
     * "John"
     * ]
     * }
     * ],
     * "gender": "male",
     * "birthDate": "1985-03-15"
     * },
     * "search": {
     * "mode": "match"
     * }
     * }
     * </code>
     *
     * @param p
     * @return given a Patient p, extract the first given name of its first name element, in other words, to extract p.name[0].given[0]
     * return empty string if first given name is not existing.
     */
    static String extractFirstName(final Patient p) {
        final String firstName = Optional.ofNullable(p)
                .map(Patient::getName)
                .orElse(ImmutableList.of())
                .stream().findFirst()
                .map(HumanName::getGiven)
                .orElse(ImmutableList.of())
                .stream()
                .findFirst()
                .map(StringType::getValueNotNull)
                .orElse("");
        return firstName;
    }


    static String extractLastName(final Patient p) {
        final String lastName = Optional.ofNullable(p)
                .map(Patient::getName)
                .orElse(ImmutableList.of())
                .stream().findFirst()
                .map(HumanName::getFamily)
                .orElse("");
        return lastName;
    }

    public static LocalDate dateToLocalDate(final Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).map(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate()).orElse(null);
    }

    public static Date localDateToDate(final LocalDate localDate) {
        return Optional.ofNullable(localDate).map(ld -> Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant())).orElse(null);
    }

    static String extractDob(final Patient p) {
        final String dob = Optional.ofNullable(p)
                .map(Patient::getBirthDate)
                .flatMap(d -> Optional.ofNullable(dateToLocalDate(d)).map(ld -> ld.format(DATE_FORMAT)))
                .orElse("");
        return dob;
    }


    public static List<String> readLines(final String file) {
        return Try.of(() -> Files.readAllLines(Paths.get(ClassLoader.getSystemResource(file).toURI())))
                .onFailure(t -> log.error("unable to read lines from file: {}", file, t))
                .getOrElse(ImmutableList.of());
    }
}
