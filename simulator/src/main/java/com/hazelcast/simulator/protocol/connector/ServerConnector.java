package com.hazelcast.simulator.protocol.connector;

import com.hazelcast.simulator.protocol.configuration.BootstrapConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.simulator.protocol.configuration.BootstrapConfiguration.DEFAULT_SHUTDOWN_QUIET_PERIOD;
import static com.hazelcast.simulator.protocol.configuration.BootstrapConfiguration.DEFAULT_SHUTDOWN_TIMEOUT;
import static java.lang.String.format;

/**
 * Server connector class for a Simulator Agent or Worker.
 */
class ServerConnector {

    private static final Logger LOGGER = Logger.getLogger(ServerConnector.class);

    private final EventLoopGroup group = new NioEventLoopGroup();

    private final BootstrapConfiguration bootstrapConfiguration;

    public ServerConnector(BootstrapConfiguration bootstrapConfiguration) {
        this.bootstrapConfiguration = bootstrapConfiguration;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(bootstrapConfiguration.getPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        bootstrapConfiguration.configurePipeline(channel.pipeline());
                    }
                });

        ChannelFuture future = bootstrap.bind().syncUninterruptibly();
        Channel channel = future.channel();

        LOGGER.info(format("%s started and listen on %s on addressLevel %s at addressIndex %d",
                ServerConnector.class.getName(), channel.localAddress(),
                bootstrapConfiguration.getAddressLevel(), bootstrapConfiguration.getAddressIndex()));
    }

    public void shutdown() {
        group.shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS).syncUninterruptibly();
    }
}
