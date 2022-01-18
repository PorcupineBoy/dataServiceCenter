package com.huaan.data.service.center.share.taskgroup;

import com.huaan.data.service.center.share.Thread.CustomThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public  class TaskGroupContainer {
    //创建阻塞队列
    private static BlockingQueue<Runnable> blockQueue = new ArrayBlockingQueue<Runnable>(200);
    public final static ThreadPoolExecutor pool = new ThreadPoolExecutor(10,50,2000, TimeUnit.MILLISECONDS,blockQueue,new CustomThreadFactory());

}
