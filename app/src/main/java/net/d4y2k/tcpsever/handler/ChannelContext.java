package net.d4y2k.tcpsever.handler;

import java.nio.channels.SocketChannel;

public interface ChannelContext {
    SocketChannel getChannel();
    void write(byte[] data);
    void close();
}
