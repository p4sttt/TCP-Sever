package net.d4y2k.tcpsever.handler;

import java.io.IOException;

public interface ChannelHandler {
    void onConnect(ChannelContext ctx) throws IOException;
    void onData(ChannelContext ctx, byte[] data, int length) throws IOException;
    void onDisconnect(ChannelContext ctx) throws IOException;
}
