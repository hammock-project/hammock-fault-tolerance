package ws.ament.hammock.ft;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RetryFallback implements FallbackHandler<String> {
    @Override
    public String handle(ExecutionContext executionContext) {
        return "fallback";
    }
}
