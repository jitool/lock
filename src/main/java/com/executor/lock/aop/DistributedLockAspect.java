package com.executor.lock.aop;

import com.alibaba.fastjson.JSON;
import com.executor.lock.annotation.ZookeeperLock;
import com.executor.lock.factory.AbstractLockFactory;
import com.executor.lock.factory.ZkDistributedLock;
import com.google.common.base.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Auther: miaoguoxin
 * @Date: 2019/3/19 0019 13:46
 * @Description:
 */
@Aspect
public class DistributedLockAspect implements ApplicationContextAware {
    private final static String LOCK_SUFFIX = "DistributedLock";
    private static ApplicationContext applicationContext;
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    @Pointcut("@annotation(com.executor.lock.annotation.ZookeeperLock)")
    public void myInfoAnnotation() {
    }

    @Around("myInfoAnnotation()&&@annotation(zookeeperLock)")
    public Object around(ProceedingJoinPoint pjp, ZookeeperLock zookeeperLock) throws Throwable {
        Object result;
        if (zookeeperLock != null) {
            AbstractLockFactory lockFactory = applicationContext.getBean(ZkDistributedLock.class);
            if (lockFactory == null) {
                throw new RuntimeException("不存在的lockFactory");
            }
            try {
                if (lockFactory.acquireDistributedLock(zookeeperLock.name(), zookeeperLock.timeUnit(),zookeeperLock.expire())) {
                    result = pjp.proceed();//执行方法
                } else {
                    result = null;
                }
                log.debug("方法执行结束");
            } finally {
                lockFactory.releaseDistributedLock(zookeeperLock.name());
                log.debug("释放锁");
            }
        } else {
            result = pjp.proceed();//执行方法
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (DistributedLockAspect.applicationContext == null) {
            DistributedLockAspect.applicationContext = applicationContext;
        }
    }

    private String createDefaultLockPath(ProceedingJoinPoint pjp) {
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();//参数
        String json = JSON.toJSONString(args);
        return json + "_" + methodName;
    }
}
