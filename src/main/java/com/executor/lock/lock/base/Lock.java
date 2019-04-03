package com.executor.lock.lock.base;

/**
 * lock 超类
 * 
 * @author limaojie
 *
 */
public interface Lock {
	/**
	 * 分布式锁 锁住
	 * 
	 * @param key
	 * @param value
	 * @param expireTime 秒
	 * @return
	 * @throws Exception 
	 */
	boolean tryLock(String key, Object value, Long expireTime) throws Exception;

	/**
	 * 分布式锁 释放
	 * 
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	void tryRelease(String key) throws Exception;

	/**
	   *   分布式锁 检查释放
	 * 
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	void checkRelease(String key,Object value) throws Exception;
}
