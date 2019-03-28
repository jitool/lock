package com.executor.lock.annotation;


import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 切入点
 * @author limaojie
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DistributedLockAnno {

    /**
     * key前缀或者key
     *
     */
    String prefix();

    /**
     * 过期秒数,默认为5秒
     * @return 轮询锁的时间
     */
    long expire() default 5;
    
    /**
     * 是否需要确认 上锁和解锁的是自己
     * 如果不是自己,则会抛错
     * @return
     */
    boolean needSureOwn() default false;
    
}
