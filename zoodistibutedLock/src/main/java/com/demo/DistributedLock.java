package com.demo;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public interface DistributedLock {
    public boolean acquire(long time, TimeUnit unit) throws InterruptedException;
    public void acquire() throws InterruptedException;
    public void release();
}
