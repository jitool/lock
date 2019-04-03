package com.executor.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.executor.lock.lock.base.Lock;
import com.executor.lock.lock.impl.DefaultRedisLockImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * 这里实例化 redisTemplate和redisson
 * 
 * @author Administrator
 *
 */
@Configuration
@ConditionalOnProperty(value="lock.enable.redis",havingValue="true")
@ConditionalOnMissingBean(Lock.class)
public class RedisLockConfig {

	public static final String RedisLockImpl = "lock.enable.redis";
	
	public static final String LOCK_WAIT_TIME= "lock.wait.time";

	private Logger log = LoggerFactory.getLogger(RedisLockConfig.class);
	@Autowired
	private RedissonProperties redissonProperties;
	@Autowired
	private Environment environment;

	@Bean
	@ConditionalOnMissingBean(RedisTemplate.class)
	public RedisTemplate redisTemplate(ApplicationContext context) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		RedisConnectionFactory connectionFactory = context.getBean(RedisConnectionFactory.class);
		template.setConnectionFactory(connectionFactory);
		// 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
		Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(mapper);
		template.setValueSerializer(serializer);
		// 使用StringRedisSerializer来序列化和反序列化redis的key值
		template.setKeySerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}
	
	@Bean
	@ConditionalOnMissingBean(RedissonClient.class)
	public RedissonClient redissonClient() {
		log.info("-----------开始加载redission客户端-----------------------");
		Config config = new Config();
		config.useSingleServer().setAddress(redissonProperties.getAddress())
				.setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
				.setDatabase(redissonProperties.getDatabase()).setDnsMonitoring(redissonProperties.isDnsMonitoring())
				.setDnsMonitoringInterval(redissonProperties.getDnsMonitoringInterval())
				.setSubscriptionConnectionMinimumIdleSize(redissonProperties.getSubscriptionConnectionMinimumIdleSize())
				.setSubscriptionConnectionPoolSize(redissonProperties.getSubscriptionConnectionPoolSize())
				.setSubscriptionsPerConnection(redissonProperties.getSubscriptionsPerConnection())
				.setClientName(redissonProperties.getClientName())
				.setFailedAttempts(redissonProperties.getFailedAttempts())
				.setRetryAttempts(redissonProperties.getRetryAttempts())
				.setRetryInterval(redissonProperties.getRetryInterval())
				.setReconnectionTimeout(redissonProperties.getReconnectionTimeout())
				.setTimeout(redissonProperties.getTimeout()).setConnectTimeout(redissonProperties.getConnectTimeout())
				.setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
				.setPingTimeout(redissonProperties.getPingTimeout()).setPassword(redissonProperties.getPassword());
		config.setThreads(redissonProperties.getThread());
		config.setEventLoopGroup(new NioEventLoopGroup());
		config.setUseLinuxNativeEpoll(false);
		RedissonClient client = Redisson.create(config);
		log.info("--------------redisson 初始化完成-----------------------");
		return client;
	}

	/**
	 * 当配置文件中存在 lock.enable.redis 才会有redis实现
	 * 
	 * @return
	 */
	@Bean("lock")
	public Lock redisLock(ApplicationContext context) {
		DefaultRedisLockImpl defaultRedisLockImpl = new DefaultRedisLockImpl();
		RedissonClient redissonClient = context.getBean(RedissonClient.class) == null ? redissonClient()
				: context.getBean(RedissonClient.class);
		RedisTemplate redisTemplate = (RedisTemplate) context.getBean("redisTemplate");
		String waitTime = environment.getProperty(LOCK_WAIT_TIME, "5");
		defaultRedisLockImpl.setWaitTime(Long.valueOf(waitTime));
		defaultRedisLockImpl.setRedisTemplate(redisTemplate);
		defaultRedisLockImpl.setRedissonClient(redissonClient);
		//redis 默认锁 生效
		return defaultRedisLockImpl;
	}

}
