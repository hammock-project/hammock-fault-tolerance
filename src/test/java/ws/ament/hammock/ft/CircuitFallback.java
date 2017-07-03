package ws.ament.hammock.ft;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CircuitFallback implements FallbackHandler<String> {
    private int invocations = 0;
    @Override
    public String handle(ExecutionContext executionContext) {
        invocations++;
        return "dummy impl";
    }
    public int getInvocations() {
        return invocations;
    }
}
