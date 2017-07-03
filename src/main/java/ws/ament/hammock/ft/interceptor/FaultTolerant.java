package ws.ament.hammock.ft.interceptor;

import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FaultTolerant {

    FaultTolerant INSTANCE = new Literal();
    class Literal extends AnnotationLiteral<FaultTolerant> implements FaultTolerant {}
}
