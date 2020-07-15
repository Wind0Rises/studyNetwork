package com.liu.study.network.reactor.server;

import com.liu.study.network.basis.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @desc
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/6/10 17:29
 */
public class ReactorMultiWorkerServer {

    /**
     *  1、 多路复用器（管理所有通道的）
     */
    private Selector selector;

    private DispatchHandler[] handlers;

    private ServerSocketChannel serverSocketChannel;



    public static void main(String[] args) {
        new Thread(new Server(8093)).start();
    }




    /**
     * Server构造函数。多路复用的。
     */
    public ReactorMultiWorkerServer(int port) {
        try {
            // 01、 创建一个Selector实例。打开多路复用器
            this.selector = Selector.open();

            // 02、 打开serverSocket通道。每一个ServerSocketChannel都一个与之对应的
            //      ServerSocket,通过其socket()方法获取。
            ServerSocketChannel channel = ServerSocketChannel.open();
            this.serverSocketChannel = channel;

            // 03、 设置模式,设置为非堵塞。
            channel.configureBlocking(false);

            // 04、 绑定地址。注意与channel.socket().bind(new InetSocketAddress(8080));
            channel.bind(new InetSocketAddress(port));

            // 05、 把ServerSocketChannel注册到Selector【服务端通道】
            //     OP_ACCEPT: 代表接受请求操作			    16
            //	   OP_CONNECT:代表连接操作				8
            //     OP_READ  : 代表读操作					1
            //     OP_WRITE : 代表写操作					4
            channel.register(this.selector, SelectionKey.OP_ACCEPT, new Acceptor(selector, channel));

            /**
             * 创建多个请求处理器。
             */
            DispatchHandler[] handlers = new DispatchHandler[20];
            for (DispatchHandler handler : handlers){
                handler = new DispatchHandler();
            }
            this.handlers = handlers;
            System.out.println("【服务已启动】,监听端口为：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     *
     */
    public void run() {
        int count = 0;

        // 无限循环，处理注册到多路复用器中的“client channel”
        while(true) {
            try {
                // 01、 启动多路复用器的监听模式
                // select()：方法会堵塞，知道至少有一个准备好的channel。如果有至少有一个准备好的channel，将可以往下执行。
                this.selector.select();
                System.out.println(selector.keys());


                // 02、 获取多路复用器中的SelectionKey。每次向Selector注册时都会创建一个SelectKey。
                //      这个时候获取到的就是准备好的Channel。
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();


                // 03、 遍历所有准备好的Channel，并根据对应的Key，选择不同的处理方式。
                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    System.out.println(key.toString());

                    if(key.isAcceptable()){
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        handlers[count ++ % 20].addChannel(socketChannel);
                    }
                    keys.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    static class DispatchHandler{
        private static Executor executor = new ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));

        private Selector selector;

        public DispatchHandler() throws IOException {
            selector = Selector.open();
            this.start();
        }

        public void addChannel(SocketChannel socketChannel) throws ClosedChannelException {
            socketChannel.register(selector, SelectionKey.OP_READ);
            this.selector.wakeup();
        }

        private void start() {
            executor.execute(() -> {
                // 无限循环，处理注册到多路复用器中的“client channel”
                while(true) {
                    try {
                        // 01、 启动多路复用器的监听模式
                        // select()：方法会堵塞，知道至少有一个准备好的channel。如果有至少有一个准备好的channel，将可以往下执行。
                        this.selector.select();
                        System.out.println(selector.keys());

                        // 02、 获取多路复用器中的SelectionKey。每次向Selector注册时都会创建一个SelectKey。
                        //      这个时候获取到的就是准备好的Channel。

                        Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();

                        // 03、 遍历所有准备好的Channel，并根据对应的Key，选择不同的处理方式。
                        while(keys.hasNext()) {
                            SelectionKey key = keys.next();
                            System.out.println(key.toString());

                            if(key.isValid()) {
                                /**
                                 * 获取attach到SelectorKey中的对象。然后运行对应的Runnable。
                                 */
                                Runnable handler = (Runnable) key.attachment();
                                handler.run();
                                keys.remove();
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }



    static class Acceptor implements Runnable {

        private Selector selector;

        private ServerSocketChannel serverSocketChannel;

        public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel) {
            this.selector = selector;
            this.serverSocketChannel = serverSocketChannel;
        }

        @Override
        public void run() {
            SocketChannel socketChannel = null;
            try {
                // 01、
                socketChannel = serverSocketChannel.accept();

                // 02、 设置为非堵塞模式
                socketChannel.configureBlocking(false);

                /**
                 * 03、 把该通道注册的多路复用器,并设置读取模式。
                 *
                 * 最后一个参数的含义：最终调用SelectorKey.attach(..)方法。
                 */
                socketChannel.register(this.selector, SelectionKey.OP_READ, new ReactorBasisServer.Reader(socketChannel));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private static Executor executors = new ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    /**
     * 读处理操作。
     */
    static class Reader implements Runnable {

        private SocketChannel socketChannel;

        public Reader(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            executors.execute(new ReactorMultiThreadServer.HandlerReader(socketChannel));
        }
    }


    /**
     * 真正的处理过程。
     */
    static class HandlerReader implements Runnable {

        private SocketChannel socketChannel;

        public HandlerReader(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            try {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                // 1、 请求read缓存
                readBuffer.clear();

                // 2、 读取socket
                int readerCount = socketChannel.read(readBuffer);
                if (readerCount == -1) {
                    socketChannel.close();
                }

                // 3、 如果有数据输入，则读取数据，buffer注意要复位
                readBuffer.flip();
                byte[] bytes = new byte[readBuffer.remaining()];

                readBuffer.get(bytes);
                String request = new String(bytes).trim();

                System.out.println("请求数据为：" + request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Writer implements Runnable {
        @Override
        public void run() {

        }
    }
}
