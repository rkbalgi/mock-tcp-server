package com.daalitoy.tools.mocktcp.server;

import com.daalitoy.tools.mocktcp.server.decoders.Mli2EFrameDecoder;
import com.daalitoy.tools.mocktcp.server.handlers.HsmRequestHandler;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Thales Mock Hsm Simulator
 */
public class MockTcpServer {
    public static void main(String[] args) {


        if (args.length != 3) {
            System.err.println("Usage: java com.americanexpress.gan.util.tools.MockTcpServer [host] [port] [delay in ms] \n Example: " +
                    "java com.americanexpress.gan.util.tools.MockTcpServer localhost 1500 200\n");
            return;

        }

        int delay = Integer.parseInt(args[2]);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.setFactory(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        serverBootstrap.getPipeline().addLast("frame-decoder", new Mli2EFrameDecoder());
        serverBootstrap.getPipeline().addLast("hsm-req-handler", new HsmRequestHandler(delay));

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        System.out.println("Starting Mock HSM simulator. " + " Host = " + host + " Port = " + port);

        serverBootstrap.bind(new InetSocketAddress(host, port));


    }
}
