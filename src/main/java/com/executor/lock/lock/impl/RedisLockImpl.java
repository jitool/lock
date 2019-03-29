package com.executor.lock.lock.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.executor.lock.lock.base.Lock;

/**
 * redis的实现
 * @author limaojie
 */
public class RedisLockImpl implements Lock{
	/**
	 * 这里就不写util了
	 */
	@Autowired
	private StringRedisTemplate redisTemplate;
	/**
	 * 假实现
	 */
	@Override
	public boolean tryLock(String key, Object value, Long expireTime) {
		try {
            RedisCallback<String> callback = (connection) -> {
            	System.out.println(connection.getNativeConnection());
            	return key;
            };
            redisTemplate.execute(callback);
            return true;
        } catch (Exception e) {
        }
		return true;
	}

	@Override
	public boolean tryRelease(String key) {
		return true;
	}

	@Override
	public boolean checkRelease(String key) {
		return true;
	}

}
