package com.liu.study.network.server;

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

				// 03、 accept()会堵塞直到有请求过来。 等待请求。
				socket = serverSocket.accept();
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
