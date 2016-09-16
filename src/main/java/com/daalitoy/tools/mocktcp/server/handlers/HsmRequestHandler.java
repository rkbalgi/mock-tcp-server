package com.daalitoy.tools.mocktcp.server.handlers;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.JdkLoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Raghavendra Balgi on 13-09-2016.
 */
public class HsmRequestHandler extends SimpleChannelHandler {

    private final int delay;
    InternalLogger logger = JdkLoggerFactory.getInstance(HsmRequestHandler.class);
    private final AtomicLong counter = new AtomicLong(0);

    public HsmRequestHandler(int delay) {
        this.delay = delay;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelConnected(ctx, e);
        logger.info(Thread.currentThread().getName()+" channel opened - " + ctx.getChannel().getRemoteAddress());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.messageReceived(ctx, e);
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();
        logger.info(Thread.currentThread().getName()+" Received Buf = " + ChannelBuffers.hexDump(buf));

        boolean shouldDelay = false;
        counter.compareAndSet(Long.MAX_VALUE, 0);

        if (counter.incrementAndGet() % 2 == 0) {
            shouldDelay = true;
        }

        byte[] header = new byte[12];
        buf.readBytes(header);
        byte[] commandBytes = new byte[2];
        buf.readBytes(commandBytes);
        String command = new String(commandBytes);
        ChannelBuffer responseBuf = ChannelBuffers.copiedBuffer(header);
        ChannelBuffer finalBuf;


        logger.info(String.format("Received - Command: %s, Header  = %s", command, new String(header)));


        switch (command) {

            case "NC": {
                //hardcoded ND response
                byte[] data = "ND0026860400000000001084-0906".getBytes("US-ASCII");
                int respSize = responseBuf.capacity() + data.length;
                finalBuf = ChannelBuffers.buffer(2 + respSize);
                finalBuf.writeShort(respSize);
                finalBuf.writeBytes(responseBuf);
                finalBuf.writeBytes(data);

                break;


            }
            case "CC": {
                //hardcoded CD response
                byte[] data = "CD0004A4D4D9C5AAFEA4E701".getBytes("US-ASCII");
                int respSize = responseBuf.capacity() + data.length;
                finalBuf = ChannelBuffers.buffer(2 + respSize);

                finalBuf.writeShort(respSize);
                finalBuf.writeBytes(responseBuf);
                finalBuf.writeBytes(data);

                ///write out
                //ctx.getChannel().write(finalBuf);
                break;


            }
            case "C2": {
                //hardcoded C3 response
                byte[] data = "C3008713FD6D7E03781F".getBytes("US-ASCII");
                int respSize = responseBuf.capacity() + data.length;
                finalBuf = ChannelBuffers.buffer(2 + respSize);

                finalBuf.writeShort(respSize);
                finalBuf.writeBytes(responseBuf);
                finalBuf.writeBytes(data);

                ///write out
                //ctx.getChannel().write(finalBuf);
                break;


            }

            default: {
                System.err.println("unsupported HSM command - " + command);
                return;
            }


        }


        if (shouldDelay) {
            Thread.sleep(delay);
        }
        ///write out
        ctx.getChannel().write(finalBuf);
    }
}