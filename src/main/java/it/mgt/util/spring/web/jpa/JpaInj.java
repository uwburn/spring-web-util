package it.mgt.util.spring.web.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JpaInj {
    
    String primaryKey() default "";
    String query() default "";
    String defaultQuery() default "";
    String[] allowedQuery() default {};
    String[] forbiddenQuery() default {};
    PathParam[] pathParams() default {};
    QueryParam[] queryParams() default {};
    boolean notFoundException() default true;

}
