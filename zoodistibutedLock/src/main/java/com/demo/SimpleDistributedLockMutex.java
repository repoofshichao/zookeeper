package com.demo;

import org.I0Itec.zkclient.ZkClient;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class SimpleDistributedLockMutex extends BaseDistributedLock implements DistributedLock {
    private String ourLockPath;
    public SimpleDistributedLockMutex(ZkClientExt client, String basePath, String lockName) {
        super(client,basePath,lockName);

    }

    public boolean internalLock(long time, TimeUnit unit) throws InterruptedException {
        ourLockPath = attemptLock(time,unit);
        return null != ourLockPath;
    }
    public boolean acquire(long time, TimeUnit unit) throws InterruptedException {
        return internalLock(time,unit);
    }

    public void acquire() throws InterruptedException {
        internalLock(-1,null);
    }

    public void release() {
        releaseLock(ourLockPath);
    }
}
