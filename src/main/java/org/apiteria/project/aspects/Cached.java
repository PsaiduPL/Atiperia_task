package org.apiteria.project.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    int size() default 25;
   ChronoUnit refreshUnit() default ChronoUnit.MINUTES;
    int refreshInterval() default 1;
}
