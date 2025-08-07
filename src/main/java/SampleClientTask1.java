import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SampleClientTask1 {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        final List<Patient> patients = Optional.ofNullable(response)
                .map(Bundle::getEntry)
                .orElse(ImmutableList.of())
                .stream()
                .filter(c -> Objects.nonNull(c) && Objects.nonNull(c.getResource()))
                .map(c -> (Patient) c.getResource())
                .sorted(Comparator.<Patient, String>comparing(Util::extractFirstName))
                .collect(Collectors.toList());

        patients.forEach(p -> log.info("patient firstName: {}, lastName: {}, dateOfBirth: {}", Util.extractFirstName(p), Util.extractLastName(p), Util.extractDob(p)));
    }

}

