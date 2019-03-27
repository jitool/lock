package com.executor.lock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: miaoguoxin
 * @Date: 2019/3/19 0019 09:51
 * @Description:
 */
@Configuration
@ConfigurationProperties(prefix = "curator")
public class CuratorProperties {

    private String connectString;
    private int sessionTimeOut = 20000;
    private int connectionTimeOut = 5000;
    private int retryTimes = 3;
    private int sleepBetweenRetryTime = 2000;


    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getSleepBetweenRetryTime() {
        return sleepBetweenRetryTime;
    }

    public void setSleepBetweenRetryTime(int sleepBetweenRetryTime) {
        this.sleepBetweenRetryTime = sleepBetweenRetryTime;
    }


    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }
}
