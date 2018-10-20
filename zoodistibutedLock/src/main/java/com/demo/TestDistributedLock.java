package com.demo;

import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class TestDistributedLock {
    public static void main(String [] args) throws InterruptedException {
        ZkClientExt client1 = new ZkClientExt("127.0.0.1:2181",5000,5000,new BytesPushThroughSerializer());

        ZkClientExt client2 = new ZkClientExt("127.0.0.1:2181",5000,5000,new BytesPushThroughSerializer());
        //BaseDistributedLock lock = new BaseDistributedLock(client,"/base","lock-");

        //List<String> list = lock.getSortedChildren();
        //for(String item : list) {
         //   System.out.println(item);
        //}
        final SimpleDistributedLockMutex mutext1 = new SimpleDistributedLockMutex(client1,"/base","lock-");
        final SimpleDistributedLockMutex mutext2 = new SimpleDistributedLockMutex(client2, "/base","lock-");

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("in thread: get lock");
                boolean isLock = false;
                try {
                    isLock = mutext2.acquire(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("in thread: get lock status:" + isLock);
                System.out.println("in thread: release lock");

                mutext2.release();
            }
        });

        System.out.println("in main:get lock");
        mutext1.acquire();
        thread1.start();
        sleep(10000);
        System.out.println("in main release lock");
        mutext1.release();


        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
