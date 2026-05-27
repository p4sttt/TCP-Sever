package net.d4y2k.tcpsever.core.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.d4y2k.tcpsever.core.BufferPoolExhaustedException;
import net.d4y2k.tcpsever.core.DirectBufferPool;
import net.d4y2k.tcpsever.core.EventLoopWorker;
import net.d4y2k.tcpsever.handler.ChannelHandler;
import net.d4y2k.tcpsever.handler.impl.TcpChannelContext;

public final class NioEventLoopWorker implements EventLoopWorker {
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoopWorker.class);

    private final Selector selector;
    private final DirectBufferPool bufferPool;
    private final Queue<SocketChannel> channelsQueue;
    private final byte[] readBuffer;
    private final ChannelHandler handler;
    
    private volatile boolean running = true;
    private volatile boolean stopping = false;
    private long stopTime = 0;

    public NioEventLoopWorker(int bufferSize, DirectBufferPool bufferPool, ChannelHandler handler) throws IOException {
        this.selector = Selector.open();
        this.bufferPool = bufferPool;
        this.channelsQueue = new ConcurrentLinkedQueue<>();
        this.readBuffer = new byte[bufferSize];
        this.handler = handler;
    }

    @Override
    public void registerNewClient(SocketChannel channel) {
        if (stopping) {
            closeChannel(channel);
            return;
        }
        channelsQueue.add(channel);
        selector.wakeup();
    }

    @Override
    public void stopGracefully() {
        this.stopping = true;
        this.stopTime = System.currentTimeMillis();
        selector.wakeup();
    }

    @Override
    public void run() {
        logger.info("NioEventLoopWorker(name={}) running successfully", Thread.currentThread().getName());

        while (running) {
            try {
                // If stopping, check active client connections
                if (stopping) {
                    boolean hasActiveClients = false;
                    for (SelectionKey key : selector.keys()) {
                        if (key.isValid() && key.channel() instanceof SocketChannel) {
                            TcpChannelContext ctx = (TcpChannelContext) key.attachment();
                            if (ctx != null && !ctx.getWriteQueue().isEmpty()) {
                                hasActiveClients = true;
                            } else {
                                // No pending writes, close client immediately during graceful shutdown
                                SocketChannel clientChan = (SocketChannel) key.channel();
                                logger.info("Graceful shutdown: closing client {} (no pending writes)", clientChan.getRemoteAddress());
                                if (ctx != null && handler != null) {
                                    handler.onDisconnect(ctx);
                                }
                                closeChannel(clientChan);
                                key.cancel();
                            }
                        }
                    }

                    // Exit immediately if no clients left or graceful drainage timeout (3000ms) exceeded
                    if (!hasActiveClients || (System.currentTimeMillis() - stopTime > 3000)) {
                        if (hasActiveClients) {
                            logger.warn("Graceful shutdown timeout exceeded. Force closing remaining connections.");
                            for (SelectionKey key : selector.keys()) {
                                if (key.isValid() && key.channel() instanceof SocketChannel) {
                                    TcpChannelContext ctx = (TcpChannelContext) key.attachment();
                                    if (ctx != null && handler != null) {
                                        handler.onDisconnect(ctx);
                                    }
                                    closeChannel((SocketChannel) key.channel());
                                    key.cancel();
                                }
                            }
                        }
                        running = false;
                        break;
                    }
                }

                // If not stopping, select blocking, else select with a quick timeout (100ms) during drainage
                int readyChannels = stopping ? selector.select(100) : selector.select();

                this.processNewClients();

                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isReadable())
                        this.processRead(key);

                    if (key.isValid() && key.isWritable())
                        this.processWrite(key);
                }

            } catch (Exception e) {
                logger.error("Error in event loop", e);
            }
        }

        this.closeWorkerResources();
    }

    private void processRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        TcpChannelContext ctx = (TcpChannelContext) key.attachment();
        ByteBuffer buffer = null;

        try {
            buffer = bufferPool.getBuffer();
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                logger.debug("Client disconnected: {}", channel.getRemoteAddress());
                if (ctx != null && handler != null) {
                    handler.onDisconnect(ctx);
                }
                closeChannel(channel);
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                buffer.get(readBuffer, 0, bytesRead);

                if (ctx != null && handler != null) {
                    handler.onData(ctx, readBuffer, bytesRead);
                }
                logger.debug("Read {} bytes from {}", bytesRead, channel.getRemoteAddress());
            }

        } catch (BufferPoolExhaustedException e) {
            logger.warn("Buffer pool exhausted, skipping read for channel");
        } catch (IOException e) {
            logger.error("Error reading from channel", e);
            if (ctx != null && handler != null) {
                try {
                    handler.onDisconnect(ctx);
                } catch (Exception ex) {
                    // Suppress nested exception
                }
            }
            closeChannel(channel);
            key.cancel();
        } finally {
            if (buffer != null) {
                bufferPool.releaseBuffer(buffer);
            }
        }
    }

    private void processWrite(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        TcpChannelContext ctx = (TcpChannelContext) key.attachment();
        if (ctx == null) {
            return;
        }

        Queue<ByteBuffer> queue = ctx.getWriteQueue();
        try {
            while (true) {
                ByteBuffer buffer = queue.peek();
                if (buffer == null) {
                    break;
                }
                channel.write(buffer);
                if (buffer.hasRemaining()) {
                    // Socket buffer full, wait for next isWritable
                    break;
                }
                queue.poll(); // Fully sent, remove from queue
            }

            if (queue.isEmpty()) {
                int ops = key.interestOps();
                key.interestOps(ops & ~SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            logger.error("Error writing to channel", e);
            if (handler != null) {
                try {
                    handler.onDisconnect(ctx);
                } catch (Exception ex) {
                    // Suppress nested exception
                }
            }
            closeChannel(channel);
            key.cancel();
        }
    }

    private void processNewClients() {
        SocketChannel channel;
        while ((channel = channelsQueue.poll()) != null) {
            try {
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                TcpChannelContext ctx = new TcpChannelContext(channel, key);
                key.attach(ctx);
                logger.debug("Registered new client");
                if (handler != null) {
                    handler.onConnect(ctx);
                }
            } catch (Exception e) {
                logger.error("Failed to register new client", e);
                closeChannel(channel);
            }
        }
    }

    private void closeChannel(SocketChannel channel) {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
        } catch (Exception e) {
            logger.error("Failed to close channel", e);
        }
    }

    private void closeWorkerResources() {
        try {
            for (SelectionKey key : selector.keys()) {
                closeChannel((SocketChannel) key.channel());
            }
            selector.close();
            logger.info("NioEventLoopWorker resources cleaned up");
        } catch (Exception e) {
            logger.error("Failed to close worker resources", e);
        }
    }
}
