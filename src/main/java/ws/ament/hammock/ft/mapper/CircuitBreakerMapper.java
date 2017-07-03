package ws.ament.hammock.ft.mapper;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import java.util.function.Function;

import static ws.ament.hammock.ft.mapper.RetryPolicyMapper.toTimeUnit;

public class CircuitBreakerMapper implements Function<CircuitBreaker,net.jodah.failsafe.CircuitBreaker>{
    @Override
    public net.jodah.failsafe.CircuitBreaker apply(CircuitBreaker circuitBreaker) {
        int failures = (int)(circuitBreaker.failureRatio() * circuitBreaker.requestVolumeThreshold());
        int successes = circuitBreaker.successThreshold() * circuitBreaker.requestVolumeThreshold();
        return new net.jodah.failsafe.CircuitBreaker()
                .failOn(circuitBreaker.failOn())
                .withDelay(circuitBreaker.delay(), toTimeUnit(circuitBreaker.delayUnit()))
                .withFailureThreshold(failures,circuitBreaker.requestVolumeThreshold())
                .withSuccessThreshold(successes, circuitBreaker.requestVolumeThreshold());
    }
}
