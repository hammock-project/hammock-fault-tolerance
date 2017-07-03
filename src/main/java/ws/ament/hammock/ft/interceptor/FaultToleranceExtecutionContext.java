package ws.ament.hammock.ft.interceptor;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

public class FaultToleranceExtecutionContext implements ExecutionContext{
    private final InvocationContext invocationContext;

    public FaultToleranceExtecutionContext(InvocationContext invocationContext) {
        this.invocationContext = invocationContext;
    }

    @Override
    public Method getMethod() {
        return invocationContext.getMethod();
    }

    @Override
    public Object[] getParameters() {
        return invocationContext.getParameters();
    }

    public InvocationContext getInvocationContext() {
        return invocationContext;
    }
}
