package com.executor.lock.factory;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: hwz
 * @Date: 2019/3/19 0019 16:29
 * @Description:
 */
public class RedissionDistributedLock extends AbstractLockFactory {


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取定时释放的锁
     * @param key 锁的key
     * @param timeUnit 定时时间类型
     * @param timeOut 时间
     * @return
     */
    @Override
    public boolean acquireDistributedLock(String key, TimeUnit timeUnit, long timeOut) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(timeOut, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 获取锁，不会自己释放，容易出现死锁
     * @param key 锁的key
     * @return
     */
    @Override
    public boolean releaseDistributedLock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        return true;
    }

    /**
     * 释放锁
     * @param key 释放锁的key
     * @return
     */
    @Override
    public boolean acquireDistributedLock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
        return true;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
}
