package merkulyevsasha.ru.annotations.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MapperJ {
    Class<?>[] oneWayMapClasses() default {};
    Class<?>[] twoWayMapClasses() default {};
}
