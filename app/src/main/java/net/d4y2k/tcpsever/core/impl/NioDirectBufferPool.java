package net.d4y2k.tcpsever.core.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.d4y2k.tcpsever.core.BufferPoolExhaustedException;
import net.d4y2k.tcpsever.core.DirectBufferPool;

public final class NioDirectBufferPool implements DirectBufferPool {
    private static final Logger logger = LoggerFactory.getLogger(NioDirectBufferPool.class);

    private final BlockingQueue<ByteBuffer> pool;
    private final int bufferSize;

    public NioDirectBufferPool(int poolSize, int bufferSize) {
        this.bufferSize = bufferSize;
        this.pool = new ArrayBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            pool.add(ByteBuffer.allocateDirect(bufferSize));
        }

        logger.info("NioDirectBufferPool(poolSize={}, bufferSize={}) allocated", poolSize, bufferSize);
    }

    @Override
    public ByteBuffer getBuffer() throws BufferPoolExhaustedException {
        ByteBuffer buffer = pool.poll();

        if (buffer == null) {
            throw new BufferPoolExhaustedException();
        }

        return buffer;
    }

    @Override
    public void releaseBuffer(ByteBuffer buffer) {
        pool.add(buffer);
    }
}
