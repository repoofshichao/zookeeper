package com.demo;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.sql.Time;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BaseDistributedLock {
    private String basePath;
    private String path;
    private String lockName;
    private ZkClientExt client;

    public BaseDistributedLock(ZkClientExt client, String basePath, String lockName) {
        this.client = client;
        this.basePath = basePath;
        this.lockName = lockName;
        this.path = basePath.concat("/").concat(lockName);
    }
    public String createLockNode(ZkClient client, String path) {
        return client.createEphemeralSequential(path,null);
    }
    private void deleteOutPath(ZkClient client, String path) {
        if (path != null) {
            client.delete(path);
        }
    }
    public boolean waitToLock(Long time, TimeUnit unit, String ourPath) throws InterruptedException {
        boolean haveTheLock = false;
        boolean doDelete = false;

        long waitTime = 0;
        if (unit != null) {
            waitTime = unit.toMillis(time);
            long curTime = System.currentTimeMillis();

        }


        while (!haveTheLock) {
            List<String> children =getSortedChildren();
            String sequenceNodename = ourPath.substring(basePath.length() + 1);

            final CountDownLatch down = new CountDownLatch(1);
            int ourIndex = children.indexOf(sequenceNodename);

            String waitPath = null;

            IZkDataListener listener = new IZkDataListener() {
                public void handleDataChange(String s, Object o) throws Exception {

                }

                public void handleDataDeleted(String s) throws Exception {
                    down.countDown();
                }
            };

            if (sequenceNodename.equals(children.get(0))) {
                haveTheLock = true;
                return haveTheLock;
            } else {
                int preIndex = ourIndex - 1;
                waitPath = children.get(preIndex);
                client.subscribeDataChanges(basePath.concat("/").concat(waitPath), listener);
            }
            try {
                if (unit == null) {
                    down.await();
                } else {
                    boolean isDone = down.await(waitTime,TimeUnit.MILLISECONDS);

                    if (!isDone) {
                        break;
                    }

                }


            } catch (InterruptedException e) {
                throw e;
            } finally {
                client.unsubscribeDataChanges(basePath.concat("/").concat(waitPath), listener);
            }


        }

        return haveTheLock;
    }

    List<String> getSortedChildren() {
        try {
            List<String> children = client.getChildren(basePath);
            Collections.sort(children, new Comparator<String>() {
                public int compare(String lhs, String rhs) {
                    return getLockNodeNumber(lhs, lockName).compareTo(getLockNodeNumber(rhs, lockName));
                }
            });
            return children;
        } catch(ZkNoNodeException e) {
            client.createPersistent(basePath,true);
            return getSortedChildren();
        }

    }

    private String getLockNodeNumber(String str, String lockName) {
        int index = str.lastIndexOf(lockName);
        if (index >= 0) {
            index = index + lockName.length();
            return index <= str.length()?str.substring(index):"";
        }
        return str;
    }

    public String attemptLock(long time, TimeUnit unit) throws InterruptedException {
        boolean getLock = false;
        boolean haveTheLock = true;
        String ourPath = null;
        while (!getLock) {
            getLock = true;
            ourPath = this.createLockNode(client,path);
            haveTheLock = this.waitToLock( time,  unit, ourPath);
        }
        return haveTheLock==true? ourPath: null;
    }
    protected void releaseLock(String path) {
        this.deleteOutPath(client,path);
    }
}
