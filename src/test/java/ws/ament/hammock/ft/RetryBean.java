package ws.ament.hammock.ft;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RetryBean {
    private int tries = 0;
    @Retry(maxRetries = 4, delay = 100L)
    public String doRetries() {
        tries++;
        if(tries == 4) {
            return "success "+tries;
        }
        else {
            throw new RuntimeException("Failed "+tries);
        }
    }

    @Retry(maxRetries = 1, abortOn = RuntimeException.class)
    public String doRetryWithAbort() {
        throw new RuntimeException("Failed 0");
    }
}
