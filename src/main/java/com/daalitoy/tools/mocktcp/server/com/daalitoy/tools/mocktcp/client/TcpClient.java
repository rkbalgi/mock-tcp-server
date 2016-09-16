package com.daalitoy.tools.mocktcp.server.com.daalitoy.tools.mocktcp.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.JdkLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Raghavendra Balgi on 16-09-2016.
 */
public class TcpClient {

    private static final InternalLogger logger = JdkLoggerFactory.getInstance(TcpClient.class);
    private static final AtomicInteger requestId = new AtomicInteger(0);
    static{

    }

    public static void main(String[] args) {


        ClientBootstrap bootstrap = new ClientBootstrap();
        bootstrap.setFactory(new NioClientSocketChannelFactory(Executors.newFixedThreadPool(2),
                Executors.newFixedThreadPool(2)));
        bootstrap.getPipeline().addLast("logging-handler", new LoggingHandler());
        bootstrap.getPipeline().addLast("response-handler", new SimpleChannelHandler() {

            @Override
            public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                super.writeRequested(ctx, e);
                logger.info(Thread.currentThread().getName()+" write requested - " + ChannelBuffers.hexDump((ChannelBuffer) e.getMessage()));
            }

            @Override
            public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
                super.writeComplete(ctx, e);
                logger.info(Thread.currentThread().getName()+" write complete. bytes written = " + e.getWrittenAmount());

            }



            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                super.messageReceived(ctx, e);
                logger.info(Thread.currentThread().getName()+" received response - " + ChannelBuffers.hexDump((ChannelBuffer) e.getMessage()));

            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                super.exceptionCaught(ctx, e);
                logger.error("error on channel -", e.getCause());
            }
        });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 1500));
        try {
            future.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("exception connecting to tcp server ", e);
            bootstrap.releaseExternalResources();
            System.exit(0);
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.getChannel().write(getNcCommand());
        }


    }

    public static ChannelBuffer getNcCommand() {

        requestId.compareAndSet(9999999, 0);
        requestId.incrementAndGet();

        ChannelBuffer buf = ChannelBuffers.buffer(2 + 12 + 2);
        buf.writeShort(buf.capacity() - 2);
        try {
            buf.writeBytes(String.format("%012dNC", requestId.get()).getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return buf;
    }
}
