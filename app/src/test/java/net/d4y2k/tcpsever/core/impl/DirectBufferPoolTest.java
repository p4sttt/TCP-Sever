package net.d4y2k.tcpsever.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import net.d4y2k.tcpsever.core.BufferPoolExhaustedException;
import net.d4y2k.tcpsever.core.DirectBufferPool;

public class DirectBufferPoolTest {

    @Test
    public void testDirectBufferPool() throws Exception {
        int poolSize = 3;
        int bufferSize = 128;
        DirectBufferPool bufferPool = new NioDirectBufferPool(poolSize, bufferSize);

        ByteBuffer buf1 = bufferPool.getBuffer();
        ByteBuffer buf2 = bufferPool.getBuffer();
        ByteBuffer buf3 = bufferPool.getBuffer();

        assertNotNull(buf1);
        assertNotNull(buf2);
        assertNotNull(buf3);
        assertTrue(buf1.isDirect());
        assertEquals(bufferSize, buf1.capacity());

        assertThrows(BufferPoolExhaustedException.class, () -> {
            bufferPool.getBuffer();
        });

        bufferPool.releaseBuffer(buf1);

        ByteBuffer bufRecycled = bufferPool.getBuffer();
        assertNotNull(bufRecycled);
        assertEquals(buf1, bufRecycled);
    }
}
