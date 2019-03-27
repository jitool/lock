package com.executor.lock.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: miaoguoxin
 * @Date: 2019/3/18 0018 17:48
 * 分布式锁的加锁释放锁接口
 */
public abstract class AbstractLockFactory {


     public static final Logger log= LoggerFactory.getLogger(AbstractLockFactory.class);

     /**
      * 获取锁
      * @param key
      * @return
      */
     public abstract boolean acquireDistributedLock(String key);

     /**
      * 获取锁
      * @param timeOut
      * @param key
      * @return
      */
     public abstract boolean acquireDistributedLock(String key, TimeUnit timeUnit, long timeOut);

     /**
      * 释放锁
      * @param
      * @return
      */
     public abstract boolean releaseDistributedLock(String key);
}
