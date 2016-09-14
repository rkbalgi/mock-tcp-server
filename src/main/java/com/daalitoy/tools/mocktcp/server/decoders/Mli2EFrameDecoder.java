package com.daalitoy.tools.mocktcp.server.decoders;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * Created by Raghavendra Balgi on 13-09-2016.
 */
public class Mli2EFrameDecoder extends FrameDecoder {
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {


        if (channelBuffer.readableBytes() < 2) {
            return null;
        } else {
            channelBuffer.markReaderIndex();
            int len = channelBuffer.readShort();
            if (channelBuffer.readableBytes() < len) {
                channelBuffer.resetReaderIndex();
                return null;
            } else {
                return channelBuffer.readBytes(len);
            }
        }
    }
}
