package merkulyevsasha.ru.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import merkulyevsasha.ru.annotations.params.Source;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Args {
    Source source() default Source.Java;
}
