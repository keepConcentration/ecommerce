package com.phm.ecommerce.common.application.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String lockKeyProvider() default "";

    long waitTime() default 10L;

    long leaseTime() default 5L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    boolean fair() default false;
}
