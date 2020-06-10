package com.liu.study.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @desc 服务端
 * @author Liuweian
 * @createTime 2019年2月22日 上午9:45:34
 */
public class Server implements Runnable{

    /**
     *  1、 多路复用器（管理所有通道的）
     */
	private Selector selector;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	
	// private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);


    public static void main(String[] args) {
        new Thread(new Server(8093)).start();
    }

    /**
     * Server构造函数。多路复用的。
     */
	public Server(int port) {
		try {
			// 01、 创建一个Selector实例。打开多路复用器
			this.selector = Selector.open();
			
			// 02、 打开serverSocket通道。每一个ServerSocketChannel都一个与之对应的
			//     ServerSocket,通过其socket()方法获取。
			ServerSocketChannel channel = ServerSocketChannel.open();
			
			// 03、 设置模式,设置为非堵塞。
			channel.configureBlocking(false);
			
			// 04、 绑定地址。注意与channel.socket().bind(new InetSocketAddress(8080));
			channel.bind(new InetSocketAddress(port));
			
			// 05、 把Selector注册到ServerSocketChannel【服务端通道】
			//     OP_ACCEPT: 代表接受请求操作
			//	   OP_CONNECT:代表连接操作
			//     OP_READ  : 代表读操作
			//     OP_WRITE : 代表写操作
			SelectionKey selectionKey = channel.register(this.selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("【服务已启动】,监听端口为：" + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * 
     */
	public void run() {
		// 无限循环，处理注册到多路复用器中的“client channel”
		while(true) {
			try {
				// 01、 启动多路复用器的监听模式
				int selectInt = this.selector.select();
				
				// 02、 获取多路复用器中的SelectionKey。每次向Seletor注册时都会创建一个SelectKey
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
				
				// 03、 获取
				while(keys.hasNext()) {
					SelectionKey key = keys.next();
					
					keys.remove();
					
					if(key.isValid()) {
						if(key.isAcceptable()) {
							this.accept(key);
						}
						
						if(key.isReadable()) {
							this.reader(key);
						}
						
						if(key.isWritable()) {
							this.wirter(key);
						}
					}
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void accept(SelectionKey key) {
		try {
			// 01、 获取ServerSocketChannel
			ServerSocketChannel channel = (ServerSocketChannel) key.channel();

			// 02、 获取SocketChannel
			SocketChannel socketChannel = channel.accept();
			
			// 03、 设置为非堵塞模式
			socketChannel.configureBlocking(false);
			
			// 04、 把该通道注册的多路复用器,并设置读取模式。
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reader(SelectionKey key) {
		try {
			// 1、 获取Socket通道
			SocketChannel socketChannel = (SocketChannel) key.channel();
			
			// 2、 请求read缓存
			this.readBuffer.clear();
			
			// 3、 读取socket
			int readerCount = socketChannel.read(readBuffer);
			if(readerCount == -1) {
				// 如果没有输入，关闭连接
				key.channel().close();
				key.cancel();
				return;
			}
			
			// 4、 如果有数据输入，则读取数据，buffer注意要复位
			this.readBuffer.flip();
			byte[] bytes = new byte[this.readBuffer.remaining()];
			
			this.readBuffer.get(bytes);
			String request = new String(bytes).trim();
			
			System.out.println("请求数据为：" + request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void wirter(SelectionKey key) {
		
	}
	

}
