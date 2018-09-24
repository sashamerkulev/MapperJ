package merkulyevsasha.ru.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {
    String stringValue() default "";
    int intValue() default 0;
    float floatValue() default 0F;
    short shortValue() default 0;
    long longValue() default 0;
    double doubleValue() default 0;
    boolean booleanValue() default false;
    byte byteValue() default 0;
}
