package it.mgt.util.spring.web.bean;

import it.mgt.util.spring.web.resolver.PathParam;
import it.mgt.util.spring.web.resolver.QueryParam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BeanMethodInj {

    Class<?> bean();
    String defaultMethod() default "";
    String[] allowedMethod() default {};
    String[] forbiddenMethod() default {};
    PathParam[] pathParams() default {};
    QueryParam[] queryParams() default {};
    boolean notFoundException() default true;

}
