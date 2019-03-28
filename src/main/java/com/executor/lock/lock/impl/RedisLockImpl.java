package com.executor.lock.lock.impl;

import com.executor.lock.lock.base.Lock;

/**
 * redis的实现
 * @author limaojie
 */
public class RedisLockImpl implements Lock{
	/**
	 * 假实现
	 */
	@Override
	public boolean tryLock(String key, Object value, Long expireTime) {
		return true;
	}

	@Override
	public boolean tryRelease(String key) {
		return true;
	}

	@Override
	public boolean checkRelease(String key, Object value) {
		return true;
	}

}
