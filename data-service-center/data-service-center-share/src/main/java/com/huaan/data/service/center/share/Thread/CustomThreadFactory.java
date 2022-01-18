package com.huaan.data.service.center.share.Thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    private AtomicInteger count=new AtomicInteger(1);
    @Override
    public Thread newThread(Runnable r) {
        Thread thread=new Thread(r);
        String threadName=CustomThreadFactory.class.getSimpleName()+count.getAndIncrement();
        thread.setName(threadName);
        return thread;
    }
}
