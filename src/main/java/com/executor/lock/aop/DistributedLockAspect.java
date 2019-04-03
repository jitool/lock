package com.executor.lock.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import com.executor.lock.annotation.DistributedLockAnno;
import com.executor.lock.deal.param.KeyParamHandler;
import com.executor.lock.exception.LockReleaseFailException;
import com.executor.lock.exception.TryLockFailException;
import com.executor.lock.lock.base.Lock;

/**
 * 非阻塞aop
 * @Auther: miaoguoxin
 * @Date: 2019/3/19 0019 13:46
 * @Description:
 */
@Aspect
public class DistributedLockAspect {

	/**
	 * 锁的实现
	 */
	private Lock lock;
	
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	@Pointcut("@annotation(com.executor.lock.annotation.DistributedLockAnno)")
	public void myInfoAnnotation() {
	}

	@Around("myInfoAnnotation()")
	public Object around(ProceedingJoinPoint jp) throws Throwable {
		//后缀
		String suffix = KeyParamHandler.getSuffix(jp);
		//得到参数上的注解
		DistributedLockAnno distributedLockAnno = ((MethodSignature) jp.getSignature()).getMethod().getAnnotation(DistributedLockAnno.class);
		//前缀
		String prefix = distributedLockAnno.prefix();
		StringBuilder keyBuilder = new StringBuilder(prefix);
		if(null != suffix)
			keyBuilder.append(suffix);
		//uuid作为解锁的依据,看是否需要强锁
		Object value="1";
		if(distributedLockAnno.needSureOwn()) {
			value=UUID.randomUUID().toString().replace("-", "");
		}
		String key = keyBuilder.toString();
		Long expireTime = distributedLockAnno.expire();
		boolean tryLock = lock.tryLock(key, value, expireTime);
		if(!tryLock)
			throw new TryLockFailException();
		//真正执行
		Object proceed = jp.proceed();
		//解锁
		if(!distributedLockAnno.needSureOwn())
			lock.tryRelease(key);
		else
			lock.checkRelease(key,value);
		return proceed;
	}
}
