package com.liu.study.network.basis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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

	/**
	 * 读取的文
	 */
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) {
        new Thread(new Server(8093)).start();
    }

    /**
     * Server构造函数。多路复用的。
     */
	public Server(int port) {
		try {
			// 01、 创建一个Selector实例。打开多路复用器。可以管理多个Channel。
			this.selector = Selector.open();
			
			// 02、 打开serverSocket通道。每一个ServerSocketChannel都一个与之对应的
			//      ServerSocket,通过其socket()方法获取。
			ServerSocketChannel channel = ServerSocketChannel.open();
			
			// 03、 设置模式,设置为非堵塞。
			channel.configureBlocking(false);
			
			// 04、 绑定地址。注意与channel.socket().bind(new InetSocketAddress(8080));
			channel.bind(new InetSocketAddress(port));
			
			// 05、 把ServerSocketChannel注册到Selector【服务端通道】
			//     OP_ACCEPT: 代表接受请求操作			   16
			//	   OP_CONNECT:代表连接操作				8
			//     OP_READ  : 代表读操作					1
			//     OP_WRITE : 代表写操作					4
			channel.register(this.selector, SelectionKey.OP_ACCEPT);
			
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
				// 01、 启动多路复用器的监听模式，准备好的channel才会去调用。
				// 			select()：方法会堵塞，直到至少有一个准备好的channel。如果有至少有一个准备好的channel，将可以往下执行。否则将一直堵塞下去。
				//   		SelectKey：表示SelectableChannel向Selector的注册的令牌。可以通过SelectKey获取Channel和Selector。
				this.selector.select();
				System.out.println("Selector中已经准备好" + selector.keys());
				
				// 02、 获取多路复用器中的SelectionKey。每次向Selector注册时都会创建一个SelectKey。
				//      这个时候获取到的就是准备好的Channel。
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();

				// 03、 遍历所有准备好的Channel，并根据对应的Key，选择不同的处理方式。
				while(keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					System.out.println(key.toString());
					
					if(key.isValid()) {
						/**
						 * ################################################
						 * # accept(..)、reader(..)、writer(..)不是异步的   #
						 * ################################################
						 */
						if(key.isAcceptable()) {
							System.out.println("============Accept===============");
							this.accept(key);
						}
						
						if(key.isReadable()) {
							System.out.println("============Read===============");
							this.reader(key);
						}
						
						if(key.isWritable()) {
							System.out.println("============Write===============");
							this.writer(key);
						}
					}
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 把OP_ACCEPT对应的Channel改为OP_READ然后注册到Selector中。
	 */
	public void accept(SelectionKey key) {
		try {
			// 01、 获取ServerSocketChannel。返回创建key对应的通道。
			ServerSocketChannel channel = (ServerSocketChannel) key.channel();

			// 02、 获取SocketChannel。
			//		接受与此通道的套接字建立的连接。这是一个新的Channel。【很重要】
			SocketChannel socketChannel = channel.accept();
			
			// 03、 设置为非堵塞模式
			socketChannel.configureBlocking(false);
			
			// 04、 把该通道注册的多路复用器,并设置读取模式。
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 *
	 * @param key
	 */
	public void reader(SelectionKey key) {
		try {
			// 1、 获取Socket通道
			SocketChannel socketChannel = (SocketChannel) key.channel();
			
			// 2、 从【socketChannel】中请求read缓存
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


			// socketChannel.register(this.selector, SelectionKey.OP_WRITE);
			System.out.println("请求数据为：" + request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void writer(SelectionKey key) {
		// 1、 获取Socket通道
		SocketChannel socketChannel = (SocketChannel) key.channel();

		String message = "this is server return";
		ByteBuffer byteBuffer = ByteBuffer.allocate(message.getBytes().length);
		byteBuffer.put(message.getBytes());

		try {
			socketChannel.write(byteBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			socketChannel.register(this.selector, SelectionKey.OP_CONNECT);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}
	

}
