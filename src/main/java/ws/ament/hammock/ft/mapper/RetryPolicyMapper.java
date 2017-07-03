package ws.ament.hammock.ft.mapper;

import net.jodah.failsafe.RetryPolicy;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RetryPolicyMapper implements Function<Retry, RetryPolicy> {
    @Override
    public RetryPolicy apply(Retry retry) {
        RetryPolicy retryPolicy = new RetryPolicy()
                .retryOn(retry.retryOn());
        if (retry.delay() > 0) {
            retryPolicy.withDelay(retry.delay(), toTimeUnit(retry.delayUnit()));
        }
        if (retry.maxDuration() > 0) {
            retryPolicy.withMaxDuration(retry.maxDuration(), toTimeUnit(retry.delayUnit()));
        }
        if (retry.jitter() > 0) {
            retryPolicy.withJitter(retry.jitter(), toTimeUnit(retry.jitterDelayUnit()));
        }
        if (retry.maxRetries() > 0) {
            retryPolicy.withMaxRetries(retry.maxRetries());
        }
        if (retry.abortOn().length > 0) {
            retryPolicy.abortOn(retry.abortOn());
        }
        return retryPolicy;
    }

    static TimeUnit toTimeUnit(ChronoUnit chronoUnit) {
        switch (chronoUnit) {
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            default:
                return TimeUnit.MILLISECONDS;
        }
    }
}
