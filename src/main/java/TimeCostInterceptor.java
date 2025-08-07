import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Interceptor
public class TimeCostInterceptor implements IClientInterceptor {

    private final boolean logEnabled;
    private final LongAdder requestAdder;
    private CopyOnWriteArrayList<Long> timeCosts;


    public TimeCostInterceptor() {
        super();
        logEnabled = false;
        requestAdder = new LongAdder();
        timeCosts = new CopyOnWriteArrayList<>();
    }

    public TimeCostInterceptor(final boolean isVerbose) {
        logEnabled = isVerbose;
        requestAdder = new LongAdder();
        timeCosts = new CopyOnWriteArrayList<>();
    }

    public void resetTimeCosts() {
        requestAdder.reset();
        timeCosts = new CopyOnWriteArrayList<>();
    }

    public Double getAverageTimeCost() {
        final double avgTimeCost = timeCosts.stream().mapToLong(l -> l).average().orElse(0d);
        return avgTimeCost;
    }

    @Override
    public void interceptRequest(final IHttpRequest theRequest) {
        if (Objects.nonNull(theRequest)) {
            requestAdder.add(1L);
            if (logEnabled) {
                log.info("request number: {} started: {}", requestAdder.sum(), theRequest.getUri());
            }
        } else {
            log.error("failed to get request");
        }
    }

    @Override
    public void interceptResponse(final IHttpResponse theResponse) throws IOException {
        if (Objects.nonNull(theResponse)) {
            if (logEnabled) {
                log.info("request number: {}, ended with status code: {}", requestAdder.sum(), theResponse.getStatus());
            }
            final Long timeCost = Try.of(() -> theResponse.getRequestStopWatch().getMillis()).getOrNull();
            timeCosts.add(timeCost);
            if (logEnabled) {
                log.info("request number: {}, time cost: {} millis", requestAdder.sum(), timeCost);
                log.info("total requests: {}, average time cost: {} millis ", requestAdder.sum(), getAverageTimeCost());
            }
        } else {
            log.error("failed to get response");
        }
    }
}
