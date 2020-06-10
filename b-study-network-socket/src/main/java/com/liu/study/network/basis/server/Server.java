package com.liu.study.network.basis.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @desc 服务端
 * @author Liuweian
 * @createTime 2019年2月22日 上午9:45:34
 */
public class Server {
	
	private static final int PORT = 8089;
	
	/**
	 * 程序入口类
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			// 01、 创建服务端Socket,与Socket类似。
			serverSocket = new ServerSocket(PORT);
			HandlerExecutorPool executorPool = new HandlerExecutorPool(50, 1000);
			Socket socket = null;
			System.out.println("【服务端】启动成功，监听端口为：" + PORT);
			
			// 02、 循环获取请求。
			while(true) {

				// 03、 accept()会堵塞直到有请求过来。 等待请求。当一个连接在处理I/O的时候，系统是阻塞的，如果是单线程的话必然就挂死在那里；
				//		当堵塞的时候CPU是可以去做其他的事情；开启多线程，就可以让CPU去处理更多的事情。所有使用多线程的本质：利用多核。当I/O
				//		阻塞系统，但CPU空闲的时候，可以利用多线程使用CPU资源。
				// 		InputStream.read()和
				socket = serverSocket.accept();


				/**
				 * 多线程一般都使用线程池，可以让线程的创建和回收成本相对较低。在活动连接数不是特别高（小于单机1000）的情况下，这种模型是比较
				 * 不错的，可以让每一个连接专注于自己的I/O并且编程模型简单，也不用过多考虑系统的过载、限流等问题。线程池本身就是一个天然的漏斗，
				 * 可以缓冲一些系统处理不了的连接或请求。
				 * 这个模型最本质的问题在于，严重依赖于线程。但线程是很"贵"的资源，主要表现在：
				 * 		1、线程的创建和销毁成本很高，在Linux这样的操作系统中，线程本质上就是一个进程。创建和销毁都是重量级的系统函数。
				 * 		2、线程本身占用较大内存，像Java的线程栈，一般至少分配512K～1M的空间，如果系统中的线程数过千，恐怕整个JVM的内存都会被吃掉一半。
				 * 		3、线程的切换成本是很高的。操作系统发生线程切换的时候，需要保留线程的上下文，然后执行系统调用。如果线程数过高，可能执行线程切换
				 * 		  的时间甚至会大于线程执行的时间，这时候带来的表现往往是系统load偏高、CPU sy使用率特别高（超过20%以上)，导致系统几乎陷入不可用的状态。
				 * 		4、容易造成锯齿状的系统负载。因为系统负载是用活动线程数或CPU核心数，一旦线程数量高但外部网络环境不是很稳定，就很容易造成大量请
				 * 	      求的结果同时返回，激活大量阻塞线程从而使系统负载压力过大。
				 */
				executorPool.execut(new ServerHandler(socket));

			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
