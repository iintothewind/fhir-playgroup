import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import java.util.List;

@Slf4j
public class SampleClientTest {

    @Test
    public void testCacheLoadPatients01() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients("Smith");
        Assertions.assertThat(patients).isNotEmpty();
        Assertions.assertThat(patients).hasSize(2);
    }

    @Test
    public void testCacheLoadPatients02() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients(null);
        Assertions.assertThat(patients).isEmpty();
    }

    @Test
    public void testCacheLoadPatients03() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients("");
        Assertions.assertThat(patients).isEmpty();
    }

    @Test
    public void testCacheLoadPatients04() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients("Smith");
        final List<Patient> cachedPatients = sampleClient.cacheLoadPatients("Smith");
        Assertions.assertThat(cachedPatients).isNotEmpty();
    }

    @Test
    public void testCacheLoadPatientsFromFile01() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatientsFromFile("names.txt");
        Assertions.assertThat(patients).isNotEmpty();
    }

    @Test
    public void testCacheLoadPatientsFromFile02() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatientsFromFile("names1.txt");
        Assertions.assertThat(patients).isEmpty();
    }

    @Test
    public void testCacheLoadPatientsFromFile03() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatientsFromFile(null);
        Assertions.assertThat(patients).isEmpty();
    }

    @Test
    public void testGetAverageTimeCost01() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients("Smith");
        final Double avgTimeCost = sampleClient.getAverageTimeCost();
        Assertions.assertThat(avgTimeCost).isGreaterThan(0d);
    }

    @Test
    public void testGetAverageTimeCost02() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatientsFromFile("names.txt");
        final Double avgTimeCost = sampleClient.getAverageTimeCost();
        Assertions.assertThat(avgTimeCost).isGreaterThan(0d);
    }

    @Test
    public void testGetAverageTimeCost03() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatients(null);
        final Double avgTimeCost = sampleClient.getAverageTimeCost();
        Assertions.assertThat(avgTimeCost).isEqualTo(0d);
    }

    @Test
    public void testGetAverageTimeCost04() {
        final SampleClient sampleClient = new SampleClient();
        final List<Patient> patients = sampleClient.cacheLoadPatientsFromFile(null);
        final Double avgTimeCost = sampleClient.getAverageTimeCost();
        Assertions.assertThat(avgTimeCost).isEqualTo(0d);
    }
}
