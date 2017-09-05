package ws.ament.hammock.ft.mp;

import org.eclipse.microprofile.fault.tolerance.tck.CircuitBreakerRetryTest;

public class MPTest extends CircuitBreakerRetryTest{
    @Override
    public void testCircuitOpenWithMultiTimeouts() {
        super.testCircuitOpenWithMultiTimeouts();
    }
}
