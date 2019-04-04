package com.executor.lock.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.executor.lock.aop.DistributedLockAspect;
import com.executor.lock.lock.base.Lock;

/**
 * @Auther: miaoguoxin
 * @Date: 2019/3/19 0019 09:53
 * @Description:
 */
@Configuration
@EnableConfigurationProperties({CuratorProperties.class,RedissonProperties.class})
public class DistributedLockAutoConfiguration {

    private Logger log= LoggerFactory.getLogger(DistributedLockAutoConfiguration.class);

    @Autowired
    private CuratorProperties curatorProperties;
    
    public static final String RedisLockImpl="lock.enable.redis";

    
    @Bean
    public DistributedLockAspect distributedLockAspect(ApplicationContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    	log.info("》》》》》》》》分布式锁注解启动中《《《《《《《《《《");
    	DistributedLockAspect aspect=new DistributedLockAspect();
    	//注入返回失败的属性
    	Lock lock = (Lock) context.getBean("lock");
    	if(null == lock)
    		throw new NullPointerException("you don`t have the impl of "+Lock.class);
    	aspect.setLock(lock);
    	log.info("》》》》》》》》分布式锁注解启动成功《《《《《《《《《《");
    	return aspect;
    }

    @Bean(initMethod = "start")
    @ConditionalOnProperty(value = "curator.connect-string")
    public CuratorFramework curatorFramework() {
        log.info("开始加载CuratorFramework客户端》》》》》");
        return CuratorFrameworkFactory.newClient(
                curatorProperties.getConnectString(),
                curatorProperties.getSessionTimeOut(),
                curatorProperties.getConnectionTimeOut(),
                new RetryNTimes(curatorProperties.getRetryTimes(), curatorProperties.getSleepBetweenRetryTime()));
    }

   

}
