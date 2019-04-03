package com.executor.lock.lock.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
   *   抽象实现
 * @author lmj
 *
 */
public abstract class AbstractLockImpl implements Lock{
	
	private Logger log= LoggerFactory.getLogger(AbstractLockImpl.class);
	/**
	   * 前置Lock
	 */
	protected void beforeLock(String key, Object value, Long expireTime) {}
	/**
	 * 锁之后
	 * @param key
	 * @param value
	 * @param expireTime
	 * @param success 成功与否
	 */
	protected void afterLock(String key, Object value, Long expireTime,boolean success) {}
	/**
	 * 解锁前
	 * @param key
	 * @param value 可能为null
	 */
	protected void beforeRelease(String key, Object value) {}
	/**
	 * 解锁后
	 * @param key
	 * @param value 可能为null
	 * @param success 成功与否
	 */
	protected void afterRelease(String key, Object value,boolean success) {}
	/**
	 * 真正上锁操作
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return
	 * @throws Exception 
	 */
	public abstract boolean lock(String key, Object value, Long expireTime) throws Exception;
	
	/**
	 * 真正解锁锁操作
	 * @param key
	 * @return
	 */
	public abstract void releaseLock(String key);
	
	/**
	 * 真正解锁锁操作-需要自检
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return
	 * @throws Exception 
	 */
	public abstract void releaseLockSureOwn(String key,Object value) throws Exception;
	
	@Override
	public final boolean tryLock(String key, Object value, Long expireTime) throws Exception {
		beforeLock(key, value, expireTime);
		boolean lock=false;
		try {
			lock = lock(key, value, expireTime);
			afterLock(key, value, expireTime, lock);
		}catch (Exception e) {
			afterLock(key, value, expireTime, lock);
			log.error("lock fail",e);
			throw e;
		}
		return lock;
	}

	@Override
	public final void tryRelease(String key) throws Exception{
		beforeRelease(key,null);
		boolean success=true;
		try {
			releaseLock(key);
			afterRelease(key, null, success);
		}catch (Exception e) {
			success=false;
			afterRelease(key,null,success);
			throw e;
		}
	}

	@Override
	public final void checkRelease(String key, Object value)  throws Exception {
		beforeRelease(key,value);
		boolean success=true;
		try {
			releaseLockSureOwn(key,value);
			afterRelease(key, value, success);
		}catch (Exception e) {
			success=false;
			afterRelease(key,value,success);
			throw e;
		}
	}

}
