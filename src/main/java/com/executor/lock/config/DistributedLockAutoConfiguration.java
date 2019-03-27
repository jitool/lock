package com.executor.lock.config;

import com.executor.lock.aop.RedisLockAspect;
import com.executor.lock.factory.AbstractLockFactory;
import com.executor.lock.factory.RedissionDistributedLock;
import com.executor.lock.factory.ZkDistributedLock;
import com.executor.lock.aop.DistributedLockAspect;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(){
        return new DistributedLockAspect();
    }

    @Bean
    @ConditionalOnMissingBean(RedisLockAspect.class)
    public RedisLockAspect redisLockAspect(){
        return new RedisLockAspect();
    }

    @Bean(initMethod = "start")
    @ConditionalOnProperty(value = "curator.connect-string")
    public CuratorFramework curatorFramework() {
        log.info("开始加载CuratorFramework客户端");
        return CuratorFrameworkFactory.newClient(
                curatorProperties.getConnectString(),
                curatorProperties.getSessionTimeOut(),
                curatorProperties.getConnectionTimeOut(),
                new RetryNTimes(curatorProperties.getRetryTimes(), curatorProperties.getSleepBetweenRetryTime()));
    }


    @Bean(name = "zkDistributedLock")
    @ConditionalOnBean(CuratorFramework.class)
    public AbstractLockFactory zkDistributedLock(){
        return new ZkDistributedLock();
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

    @Bean(name = "redissionDistributedLock")
    @ConditionalOnBean(RedissonClient.class)
    public AbstractLockFactory redissonLock(){
        return new RedissionDistributedLock();
    }


}
