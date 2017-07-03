package ws.ament.hammock.ft;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CircuitBreakerBean {
    private static final int MAX_TRIES = 5;
    private int count = 0;
    private int invocations = 0;
    @CircuitBreaker(requestVolumeThreshold = MAX_TRIES, failureRatio = 0.2)
    @Fallback(CircuitFallback.class)
    public String doCircuits() {
        invocations++;
        try {
            if (count < MAX_TRIES) {
                throw new RuntimeException("I'm broken.");
            }
            else {
                return "current count "+count;
            }
        }
        finally {
            count++;
        }
    }
    public int getInvocations() {
        return invocations;
    }
}
