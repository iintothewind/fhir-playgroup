import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SampleClient {
    private final LoadingCache<String, List<Patient>> patientCache;
    private final IGenericClient client;
    private final TimeCostInterceptor timeCostInterceptor;

    public SampleClient() {
        final FhirContext fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        timeCostInterceptor = new TimeCostInterceptor(false);
        client.registerInterceptor(timeCostInterceptor);
        patientCache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30L))
                .build(new CacheLoader<String, List<Patient>>() {
                    @Override
                    public List<Patient> load(final String lastName) throws Exception {
                        return searchPatientsByLastName(client, lastName);
                    }
                });
    }

    private static List<Patient> searchPatientsByLastName(@NonNull final IGenericClient client, final String lastName) {
        log.info("searchPatientsByLastName, lastName: {}", lastName);

        if (Objects.nonNull(lastName) && !lastName.trim().isEmpty()) {
            Bundle response = client
                    .search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(lastName))
                    .returnBundle(Bundle.class)
                    .cacheControl(new CacheControlDirective().setMaxResults(999).setNoCache(false).setNoStore(false))
                    .execute();

            final List<Patient> patients = Optional.ofNullable(response)
                    .map(Bundle::getEntry)
                    .orElse(ImmutableList.of())
                    .stream()
                    .filter(c -> Objects.nonNull(c) && Objects.nonNull(c.getResource()))
                    .map(c -> (Patient) c.getResource())
                    .sorted(Comparator.<Patient, String>comparing(Util::extractFirstName))
                    .collect(Collectors.toList());
            return patients;
        }

        return ImmutableList.of();
    }

    private static List<Patient> searchPatientsFromFile(@NonNull final IGenericClient client, final String filePath) {
        log.info("searchPatientsFromFile, filePath: {}", filePath);

        final List<String> lastNames = Util.readLines(filePath);
        final List<Patient> patients = lastNames.stream()
                .flatMap(name -> searchPatientsByLastName(client, name).stream())
                .sorted(Comparator.comparing(Util::extractLastName).thenComparing(Util::extractFirstName))
                .collect(Collectors.toList());
        return patients;
    }

    public Double getAverageTimeCost() {
        return timeCostInterceptor.getAverageTimeCost();
    }

    public void resetTimeCosts() {
        timeCostInterceptor.resetTimeCosts();
    }

    public List<Patient> cacheLoadPatients(final String lastName) {
        return Try.of(() -> patientCache.get(lastName)).getOrElse(ImmutableList.of());
    }

    public List<Patient> cacheLoadPatientsFromFile(final String filePath) {
        log.info("searchPatientsFromFile, filePath: {}", filePath);

        final List<String> lastNames = Util.readLines(filePath);
        final List<Patient> patients = lastNames.stream()
                .flatMap(name -> cacheLoadPatients(name).stream())
                .sorted(Comparator.comparing(Util::extractLastName).thenComparing(Util::extractFirstName))
                .collect(Collectors.toList());
        return patients;
    }

    public static void main(String[] args) {
        // Create a FHIR client
        final SampleClient sampleClient = new SampleClient();

        final List<Patient> patients1 = sampleClient.cacheLoadPatientsFromFile("names.txt");
        log.info("average time cost for first loop: {} millis", sampleClient.getAverageTimeCost());
        sampleClient.resetTimeCosts();


        final List<Patient> patients2 = sampleClient.cacheLoadPatientsFromFile("names.txt");
        log.info("average time cost for second loop: {} millis", sampleClient.getAverageTimeCost());
        sampleClient.resetTimeCosts();

        // invalid cache
        // sampleClient.patientCache.invalidateAll();
        log.info("sleep 30 seconds till cache invalidated");
        Try.run(() -> TimeUnit.SECONDS.sleep(30L));


        final List<Patient> patients3 = sampleClient.cacheLoadPatientsFromFile("names.txt");
        log.info("average time cost for third loop: {} millis", sampleClient.getAverageTimeCost());
        sampleClient.resetTimeCosts();
    }
}
