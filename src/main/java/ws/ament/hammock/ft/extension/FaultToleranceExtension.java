package ws.ament.hammock.ft.extension;


import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import ws.ament.hammock.ft.interceptor.FaultTolerant;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import static ws.ament.hammock.ft.interceptor.FaultTolerant.INSTANCE;

public class FaultToleranceExtension implements Extension {
    public void findFaultTolerantBeans(@Observes @WithAnnotations({Retry.class, CircuitBreaker.class, Asynchronous.class})
                                       ProcessAnnotatedType<?> pat) {
        if(!pat.getAnnotatedType().isAnnotationPresent(FaultTolerant.class)) {
            pat.setAnnotatedType(new FaultTolerantAnnotatedTypeWrapper(pat.getAnnotatedType()));
        }
    }


    private static class FaultTolerantAnnotatedTypeWrapper<X> implements AnnotatedType<X> {

        private final AnnotatedType<X> delegate;

        private FaultTolerantAnnotatedTypeWrapper(AnnotatedType<X> delegate) {
            this.delegate = delegate;
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
            return annotationType == FaultTolerant.class ? (T) INSTANCE : delegate.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            Set<Annotation> annotations = delegate.getAnnotations();
            Set<Annotation> allAnotations = new LinkedHashSet<>();
            allAnotations.add(INSTANCE);
            allAnotations.addAll(annotations);
            return allAnotations;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotationType == FaultTolerant.class || delegate.isAnnotationPresent(annotationType);
        }
    }
}
