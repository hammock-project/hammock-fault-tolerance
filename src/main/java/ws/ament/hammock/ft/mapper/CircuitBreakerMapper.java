package ws.ament.hammock.ft.mapper;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.util.function.BiFunction;
import java.util.function.Function;

import static ws.ament.hammock.ft.mapper.RetryPolicyMapper.toTimeUnit;

public class CircuitBreakerMapper implements BiFunction<CircuitBreaker, Timeout,net.jodah.failsafe.CircuitBreaker> {
    @Override
    public net.jodah.failsafe.CircuitBreaker apply(CircuitBreaker circuitBreaker, Timeout timeout) {
        int failures = (int)(circuitBreaker.failureRatio() * circuitBreaker.requestVolumeThreshold());
        net.jodah.failsafe.CircuitBreaker cb = new net.jodah.failsafe.CircuitBreaker()
                .failOn(circuitBreaker.failOn())
                .withDelay(circuitBreaker.delay(), toTimeUnit(circuitBreaker.delayUnit()))
                .withFailureThreshold(failures, circuitBreaker.requestVolumeThreshold())
                .withSuccessThreshold(circuitBreaker.successThreshold());
        if(timeout != null) {
            cb.withTimeout(timeout.value(), toTimeUnit(timeout.unit()));
        }
        return cb;
    }
}
