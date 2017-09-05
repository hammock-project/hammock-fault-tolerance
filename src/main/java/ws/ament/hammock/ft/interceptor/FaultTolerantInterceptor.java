package ws.ament.hammock.ft.interceptor;

import net.jodah.failsafe.AsyncFailsafe;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.SyncFailsafe;
import net.jodah.failsafe.function.CheckedFunction;
import net.jodah.failsafe.util.Duration;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import ws.ament.hammock.ft.mapper.CircuitBreakerMapper;
import ws.ament.hammock.ft.mapper.RetryPolicyMapper;
import ws.ament.hammock.ft.util.AnnotationUtil;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Interceptor
@FaultTolerant
@Priority(400)
public class FaultTolerantInterceptor {
    private static final CircuitBreakerMapper circuitBreakerMapper = new CircuitBreakerMapper();
    private static final RetryPolicyMapper retryPolicyMapper = new RetryPolicyMapper();
    private static final Map<Method, FaultToleranceContext> contexts = new HashMap<>();

    @AroundInvoke
    public Object failGracefully(InvocationContext invocationContext) throws Exception{
        FaultToleranceContext ctx = contexts.computeIfAbsent(invocationContext.getMethod(), FaultToleranceContext::new);
        if(ctx.isRetryable() || ctx.isCircuitBreaker()) {
            return ctx.invoke(invocationContext);
        }
        return invocationContext.proceed();
    }

    private final class FaultToleranceContext {
        private final Method method;
        private net.jodah.failsafe.CircuitBreaker circuitBreaker;
        private RetryPolicy retryPolicy;
        private Class<? extends FallbackHandler> fallbackHandlerClass;

        FaultToleranceContext(Method method) {
            this.method = method;
            this.retryPolicy = isRetryable() ? getRetryPolicy() : null;
            this.circuitBreaker = isCircuitBreaker() ? getCircuitBreaker() : null;
            this.fallbackHandlerClass = getFallbackHandlerClass();
        }

        boolean isRetryable() {
            return AnnotationUtil.getAnnotation(this.method, Retry.class) != null;
        }

        boolean isCircuitBreaker() {
            return AnnotationUtil.getAnnotation(this.method, CircuitBreaker.class) != null;
        }

        Class<? extends FallbackHandler> getFallbackHandlerClass() {
            Fallback fallback = this.method.getAnnotation(Fallback.class);
            if(fallback != null) {
                return fallback.value();
            }
            else {
                return null;
            }
        }

        RetryPolicy getRetryPolicy() {
            Retry retry = AnnotationUtil.getAnnotation(method, Retry.class);
            return retryPolicyMapper.apply(retry);
        }

        net.jodah.failsafe.CircuitBreaker getCircuitBreaker() {
            CircuitBreaker circuitBreaker = AnnotationUtil.getAnnotation(method, CircuitBreaker.class);
            Timeout timeout = AnnotationUtil.getAnnotation(method, Timeout.class);
            return circuitBreakerMapper.apply(circuitBreaker,timeout);
        }

        boolean isAsync() {
            return AnnotationUtil.getAnnotation(method, Asynchronous.class) != null
                    || AnnotationUtil.getAnnotation(method, Timeout.class) != null;
        }

        Object invoke(InvocationContext invocationContext) {
            SyncFailsafe<?> failsafe = null;
            AsyncFailsafe<?> asyncFailsafe = null;
            if(this.isRetryable()) {
                failsafe = Failsafe.with(this.retryPolicy);
            }
            if(this.isCircuitBreaker()) {
                if(failsafe == null) {
                    failsafe = Failsafe.with(this.circuitBreaker);
                }
                else {
                    failsafe.with(this.circuitBreaker);
                }
            }
            if(failsafe == null) {
                throw new IllegalStateException("Failsafe not properly configured, no Retry or CircuitBreaker defined");
            }
            if(fallbackHandlerClass != null) {
                failsafe.withFallback(new FallbackCheckedFunction<>(fallbackHandlerClass, invocationContext));
            }
            if(isAsync()) {
                asyncFailsafe = failsafe.with(new ScheduledThreadPoolExecutor(2));
            }
            try {
                if (asyncFailsafe != null) {
                    Duration timeout = circuitBreaker.getTimeout();
                    if (timeout != null) {
                        return asyncFailsafe.get(invocationContext::proceed).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                    }
                    else {
                        return asyncFailsafe.get(invocationContext::proceed).get();
                    }
                }
                else {
                    return failsafe.get(invocationContext::proceed);
                }
            }
            catch (CircuitBreakerOpenException e) {
                throw new org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException(e);
            }
            catch (TimeoutException | InterruptedException | ExecutionException e) {
                throw new org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException(e);
            }
        }
    }

    private static class FallbackCheckedFunction<X> implements CheckedFunction<Throwable, X> {
        private final Class<? extends FallbackHandler> fallbackHandlerClass;
        private final InvocationContext invocationContext;

        public FallbackCheckedFunction(Class<? extends FallbackHandler> fallbackHandlerClass,
                                       InvocationContext invocationContext) {
            this.fallbackHandlerClass = fallbackHandlerClass;
            this.invocationContext = invocationContext;
        }

        FallbackHandler lookupFallback() {
            return CDI.current().select(fallbackHandlerClass).get();
        }
        @Override
        public X apply(Throwable throwable) throws Exception {
            return (X)lookupFallback().handle(new FaultToleranceExtecutionContext(invocationContext));
        }
    }
}
