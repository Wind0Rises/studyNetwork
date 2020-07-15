package com.liu.study.network.basis.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @desc 客户端
 * @author Liuweian
 * @createTime 2019年2月22日 下午4:48:45
 */
public class Client {

    public final static String ADDRESS = "127.0.0.1";

	public final static int PORT = 8089;

	/**
	 * socket的几种状态；
	 * 		create：在创建SocketImpl成功设置为true；
	 *		bound：在绑定地址以后，bound社会为true。
	 *		connect：链接完成，返回true。
	 * @param args
	 */
	public static void main(String[] args) {
		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		
		try {
			//socket = new Socket(ADDRESS, PORT);
			socket = new Socket();
			socket.connect(new InetSocketAddress(ADDRESS, PORT));

			/**
			 *获取输出流，并写出数据。
			 */
			writer = new PrintWriter(socket.getOutputStream(), true);
			writer.println("=========   客户端发送到服务端的message   ============");

			/**
			 * 获取输入流，并读取数据。
			 */
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String response = reader.readLine();
			System.out.println("【客户端】接受到的信息：" + response);
			
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null){
				try {
					reader.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			if(writer != null){
				try {
					writer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if(socket != null){
				try {
					socket.close();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
			socket = null;				
		}
	}
}
