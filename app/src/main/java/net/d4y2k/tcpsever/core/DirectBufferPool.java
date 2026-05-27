package net.d4y2k.tcpsever.core;

import java.nio.ByteBuffer;

public interface DirectBufferPool {
    ByteBuffer getBuffer() throws BufferPoolExhaustedException;
    void releaseBuffer(ByteBuffer buffer);
}
