package com.liu.study.network.basis.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @desc 
 * @author Liuweian
 * @createTime 2019年2月22日 下午4:48:45
 */
public class SecondClient {
	
	public static void main(String[] args) {
		
		SocketChannel channel = null;
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		try {
			// 2、打开socket通道
			channel = SocketChannel.open();
			
			// 3、进行连接
			channel.connect(new InetSocketAddress("127.0.0.1", 8093));
			
			while(true){
				// 4、定义一个字节数组，然后使用系统录入功能：
				byte[] bytes = new byte[1024];
				System.in.read(bytes);
				
				// 5、 把数据放到缓冲区中
				buffer.put(bytes);
				// 6、 对缓冲区进行复位
				buffer.flip();
				// 7、 写出数据
				channel.write(buffer);
				// 8、清空缓冲区数据
				buffer.clear();

				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				channel.read(readBuffer);
				System.out.println(readBuffer.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(channel != null){
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
