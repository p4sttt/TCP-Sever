package net.d4y2k.tcpsever.handler.impl;

import java.io.IOException;

import net.d4y2k.tcpsever.handler.ChannelContext;
import net.d4y2k.tcpsever.handler.ChannelHandler;

public final class EchoChannelHandler implements ChannelHandler {
    @Override
    public void onConnect(ChannelContext ctx) throws IOException {
        // Log or handle connect event if needed
    }

    @Override
    public void onData(ChannelContext ctx, byte[] data, int length) throws IOException {
        byte[] echoData = new byte[length];
        System.arraycopy(data, 0, echoData, 0, length);
        ctx.write(echoData);
    }

    @Override
    public void onDisconnect(ChannelContext ctx) throws IOException {
        // Log or handle disconnect event if needed
    }
}
