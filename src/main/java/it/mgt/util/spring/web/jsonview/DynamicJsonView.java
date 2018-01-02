package it.mgt.util.spring.web.jsonview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicJsonView {

    Class<?> defaultView() default Class.class;
    Class<?>[] allowedView() default {};
    Class<?>[] forbiddenView() default {};

}
