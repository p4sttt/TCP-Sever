package net.d4y2k.tcpsever.core;

import java.nio.channels.SocketChannel;

public interface EventLoopWorker extends Runnable {
    void registerNewClient(SocketChannel channel);
    void stopGracefully();
}
