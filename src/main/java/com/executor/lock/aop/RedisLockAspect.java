package com.executor.lock.aop;



import com.executor.lock.annotation.RedisLock;
import com.executor.lock.factory.AbstractLockFactory;
import com.executor.lock.factory.RedissionDistributedLock;
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
 * @Auther: hwz
 */
@Aspect
public class RedisLockAspect implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(RedisLockAspect.class);
    private static ApplicationContext applicationContext;

    @Pointcut("@annotation(com.executor.lock.annotation.RedisLock)")
    public void myInfoAnnotation() {


    }
    @Around("myInfoAnnotation()&&@annotation(redisLock)")
    public Object around(ProceedingJoinPoint pjp, RedisLock redisLock) throws Throwable {
        Object result = null;
        if (redisLock != null) {
            AbstractLockFactory lockFactory = applicationContext.getBean(RedissionDistributedLock.class);
                if (lockFactory==null) {
                    throw new RuntimeException("not found bean name is redissionDistributedLock");
                }
                if(((RedissionDistributedLock) lockFactory).getRedissonClient()==null){
                    throw new RuntimeException("not found bean name is redissonClient");
                }
                if (redisLock.name() == null || redisLock.name().length() < 1) {
                    result = pjp.proceed();
                    logger.error(" create redis lock failure, name is null");
                } else {
                    //获取redis锁
                    if(lockFactory.acquireDistributedLock(redisLock.name(),redisLock.timeUnit(),redisLock.expire())){
                        logger.info(" create redis lock name is " + redisLock.name());
                        result = pjp.proceed();
                    }else{
                        logger.error(" create redis lock failure ");
                    }
                    if(lockFactory.releaseDistributedLock(redisLock.name())){
                        //释放锁
                        logger.info(" unlock redis name is " + redisLock.name());
                    }else{
                        //释放锁
                        logger.info(" unlock redis failure");
                    }

                }
            } else {
                //执行原有方法，不添加增强
                result = pjp.proceed();
            }
            return result;

    }

    @Override
    public void setApplicationContext(ApplicationContext  applicationContext) throws BeansException {
        if (RedisLockAspect.applicationContext == null) {
            RedisLockAspect.applicationContext = applicationContext;
        }
    }

}
