package merkulyevsasha.ru.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MapperJ {
    Source source() default Source.Java;
    Class<?>[] oneWayMapClasses() default {};
    Class<?>[] twoWayMapClasses() default {};
}
