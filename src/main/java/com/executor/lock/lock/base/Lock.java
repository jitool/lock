package com.executor.lock.lock.base;

/**
 * lock 超类
 * @author limaojie
 *
 */
public interface Lock {
	/**
	 * 分布式锁 锁住
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return
	 */
	boolean tryLock(String key,Object value,Long expireTime);
	/**
	 * 分布式锁 释放
	 * @param key
	 * @return
	 */
	boolean tryRelease(String key);
	/**
	 * 分布式锁 检查释放
	 * @param key
	 * @return
	 */
	boolean checkRelease(String key,Object value);
}
