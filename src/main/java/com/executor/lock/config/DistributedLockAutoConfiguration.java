package com.executor.lock.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.internal.runners.statements.Fail;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.executor.lock.aop.DistributedLockAspect;
import com.executor.lock.lock.base.Lock;
import com.executor.lock.lock.impl.RedisLockImpl;

import io.netty.channel.nio.NioEventLoopGroup;

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
    @Autowired
    private RedissonProperties redissonProperties;
    @Autowired
    private Environment environment;
    
    private static final String RedisLockImpl="lock.enable.redis";
    
    @Bean
    public DistributedLockAspect distributedLockAspect(ApplicationContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    	DistributedLockAspect aspect=new DistributedLockAspect();
    	//注入返回失败的属性
    	Lock lock = (Lock) context.getBean("lock");
    	if(null == lock)
    		throw new NullPointerException("you don`t have the impl of "+Lock.class);
    	aspect.setLock(lock);
    	log.info("分布式锁注解启动成功");
    	return aspect;
    }
    /**
     * 当配置文件中存在 lock.enable.redis 才会有redis实现
     * @return
     */
    @Bean("lock")
    @ConditionalOnProperty(name=RedisLockImpl,havingValue="true")
    public Lock redisLock() {
    	return new RedisLockImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(){
        return new DistributedLockAspect();
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

    @Bean
    @ConditionalOnProperty(value = "redisson.address")
    RedissonClient redissonClient(){
        log.info("开始加载redission客户端");
        Config config=new Config();
        config.useSingleServer()
                .setAddress(redissonProperties.getAddress())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setDatabase(redissonProperties.getDatabase())
                .setDnsMonitoring(redissonProperties.isDnsMonitoring())
                .setDnsMonitoringInterval(redissonProperties.getDnsMonitoringInterval())
                .setSubscriptionConnectionMinimumIdleSize(redissonProperties.getSubscriptionConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(redissonProperties.getSubscriptionConnectionPoolSize())
                .setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection())
                .setClientName(redissonProperties.getClientName())
                .setFailedAttempts(redissonProperties.getFailedAttempts())
                .setRetryAttempts(redissonProperties.getRetryAttempts())
                .setRetryInterval(redissonProperties.getRetryInterval())
                .setReconnectionTimeout(redissonProperties.getReconnectionTimeout())
                .setTimeout(redissonProperties.getTimeout())
                .setConnectTimeout(redissonProperties.getConnectTimeout())
                .setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
                .setPingTimeout(redissonProperties.getPingTimeout())
                .setPassword(redissonProperties.getPassword());
        config.setThreads(redissonProperties.getThread());
        config.setEventLoopGroup(new NioEventLoopGroup());
        config.setUseLinuxNativeEpoll(false);
        return Redisson.create(config);
    }

}
