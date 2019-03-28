package com.executor.lock.annotation;


import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 key参数
 * @author limaojie
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LockKeyParam {
	/**
	 * 参数标明
	 * 通过反射去获取
	 * @return
	 */
	String[] value() default {};

}
