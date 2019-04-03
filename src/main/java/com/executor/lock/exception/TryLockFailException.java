package com.executor.lock.exception;

/**
 *      上锁失败异常
 * @author limaojie
 *
 */
public class TryLockFailException extends RuntimeException{
	
	public TryLockFailException(String message) {
		super(message);
	}

	public TryLockFailException() {
	}
}
