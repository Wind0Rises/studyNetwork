package com.liu.study.network.basis.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 服务处理请求，处理器。
 *
 */
public class ServerHandler implements Runnable {
	
	private Socket socket;
	
	public ServerHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			writer = new PrintWriter(this.socket.getOutputStream(), true);
			
			String body;

			while(true){
				body = reader.readLine();
				if(body == null) break;
				System.out.println("【服务端】获取的内容: " + body);
				writer.println("Server response：请求完成。");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(writer != null) {
				writer.close();
			}
		}
	}

}
