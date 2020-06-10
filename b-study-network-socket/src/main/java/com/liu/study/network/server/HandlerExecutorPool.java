package com.liu.study.network.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @desc 处理请求连接池
 * @author Liuweian
 * @createTime 2019年2月26日 下午1:55:26
 * @version 1.0.0
 */
public class HandlerExecutorPool {
	
	private ExecutorService executorService;
	
	public HandlerExecutorPool(int maxPoolSize, int queueSize) {
		this.executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 
				maxPoolSize, 
				120L, 
				TimeUnit.SECONDS, 
				new ArrayBlockingQueue<Runnable>(queueSize));
	}

	public void execut(Runnable runnable) {
		executorService.execute(runnable);
	}
	
}
