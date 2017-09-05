package ws.ament.hammock.ft.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
        T annotation = method.getAnnotation(clazz);
        if (annotation != null) {
            return annotation;
        }
        else {
            return method.getDeclaringClass().getAnnotation(clazz);
        }
    }
}
