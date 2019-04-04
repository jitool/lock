package com.executor.lock.lock.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;

import com.executor.lock.exception.LockReleaseFailException;
import com.executor.lock.lock.base.AbstractLockImpl;

/**
 * redis的默认实现——目前依赖redisson实现分布式锁
 * @author limaojie
 */
public class DefaultRedisLockImpl extends AbstractLockImpl{
	/**
	 * 这里就不写util了
	 */
	private RedissonClient redissonClient;
	
	private RScript script;
	
	private String id;
	
	private static final  String scriptTxt = "if redis.call('HGET', KEYS[1],KEYS[2]) == nil then return 0 else return redis.call('HDEL', KEYS[1],KEYS[2]) end";
	//------------注入------------
	public void setRedissonClient(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}
	public void init() {
		script=redissonClient.getScript();
	}
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}
	//------------------注入完成------------

	/**
	 * 等待锁的时间，单位秒,默认 5
	 */
	private long waitTime;
	
	@Override
	public boolean lock(String key, Object value, Long expireTime) throws InterruptedException {
		RedissonLock lock = (RedissonLock) redissonClient.getLock(key);
		return lock.tryLock(waitTime, expireTime, TimeUnit.SECONDS);
	}

	@Override
	public void releaseLock(String key) {
		RLock lock = redissonClient.getLock(key);
		lock.unlock();
	}

	@Override
	public void releaseLockSureOwn(String key, Object value) throws Exception {
		String hash_key = getUUIDFromRedisson(Thread.currentThread().getId());
		script = redissonClient.getScript();
		List<Object> keys=new LinkedList<>();
		keys.add(key);
		keys.add(hash_key);
		long result = script.eval(Mode.READ_WRITE, scriptTxt, ReturnType.INTEGER,keys);
		if(1L != result)
			throw new LockReleaseFailException("key:"+key+" is expired");
	}
	/**
	 * 通过反射去获取redisson中的UUID
	 * @param id
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private String getUUIDFromRedisson(long tid) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(null == id) {
			Class<? extends RedissonClient> clazz = redissonClient.getClass();
			Field uUIDField = clazz.getDeclaredField("id");
			uUIDField.setAccessible(true);
			id = new StringBuilder(uUIDField.get(redissonClient).toString()).append(":").toString();
			uUIDField.setAccessible(false);
		}
		return new StringBuilder(id).append(tid).toString();
	}
}
