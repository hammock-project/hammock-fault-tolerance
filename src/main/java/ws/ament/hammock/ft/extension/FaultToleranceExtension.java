package ws.ament.hammock.ft.extension;


import org.eclipse.microprofile.faulttolerance.*;
import ws.ament.hammock.ft.interceptor.FaultTolerant;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static ws.ament.hammock.ft.interceptor.FaultTolerant.INSTANCE;

public class FaultToleranceExtension implements Extension {

    public void findFaultTolerantBeans(@Observes @WithAnnotations({Retry.class, CircuitBreaker.class, Asynchronous.class})
                                               ProcessAnnotatedType<?> pat) {
        if (!pat.getAnnotatedType().isAnnotationPresent(FaultTolerant.class)) {
            pat.setAnnotatedType(new FaultTolerantAnnotatedTypeWrapper(pat.getAnnotatedType()));
        }
    }

    private final List<Exception> failures = new ArrayList<>();

    public void verifyFallBacks(@Observes @WithAnnotations({Fallback.class}) ProcessAnnotatedType<?> pat) throws Exception {
        pat.getAnnotatedType().getMethods().stream().filter(m -> m.isAnnotationPresent(Fallback.class)).forEach(m -> {
            Fallback fallback = m.getAnnotation(Fallback.class);
            try {
                Method handle = fallback.value().getMethod("handle", ExecutionContext.class);
                if (!handle.getReturnType().equals(m.getJavaMember().getReturnType())) {
                    failures.add(new IllegalStateException("The method " + handle + " returns a different type than " + m.getJavaMember()));
                }
            } catch (NoSuchMethodException ignore) {

            }
        });
    }

    public void afterDeployValidation(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        failures.forEach(afterDeploymentValidation::addDeploymentProblem);
    }

    private static class FaultTolerantAnnotatedTypeWrapper<X> implements AnnotatedType<X> {

        private final AnnotatedType<X> delegate;
        private final Set<Annotation> annotations;

        private FaultTolerantAnnotatedTypeWrapper(AnnotatedType<X> delegate) {
            this.delegate = delegate;
            Set<Annotation> annotations = delegate.getAnnotations();
            Set<Annotation> allAnotations = new LinkedHashSet<>();
            allAnotations.add(INSTANCE);
            allAnotations.addAll(annotations);
            this.annotations = allAnotations;
        }

        @Override
        public Class<X> getJavaClass() {
            return delegate.getJavaClass();
        }

        @Override
        public Set<AnnotatedConstructor<X>> getConstructors() {
            return delegate.getConstructors();
        }

        @Override
        public Set<AnnotatedMethod<? super X>> getMethods() {
            return delegate.getMethods();
        }

        @Override
        public Set<AnnotatedField<? super X>> getFields() {
            return delegate.getFields();
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return FaultTolerant.class.equals(annotationType) ? (T) INSTANCE : delegate.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return Collections.unmodifiableSet(annotations);
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return FaultTolerant.class.equals(annotationType) || delegate.isAnnotationPresent(annotationType);
        }
    }
}
