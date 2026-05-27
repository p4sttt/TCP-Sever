package net.d4y2k.tcpsever.handler.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.d4y2k.tcpsever.handler.ChannelContext;

public final class TcpChannelContext implements ChannelContext {
    private final SocketChannel channel;
    private final SelectionKey key;
    private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();

    public TcpChannelContext(SocketChannel channel, SelectionKey key) {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void write(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        writeQueue.add(ByteBuffer.wrap(data));
        
        try {
            int ops = key.interestOps();
            if ((ops & SelectionKey.OP_WRITE) == 0) {
                key.interestOps(ops | SelectionKey.OP_WRITE);
                key.selector().wakeup();
            }
        } catch (Exception e) {
            // Ignore if key or selector is closed
        }
    }

    @Override
    public void close() {
        try {
            key.cancel();
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            // Suppress exception on closing
        }
    }

    public Queue<ByteBuffer> getWriteQueue() {
        return writeQueue;
    }
}
