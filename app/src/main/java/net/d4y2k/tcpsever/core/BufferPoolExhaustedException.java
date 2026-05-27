package net.d4y2k.tcpsever.core;

public class BufferPoolExhaustedException extends Exception {
    public BufferPoolExhaustedException(String message) {
        super(message);
    }

    public BufferPoolExhaustedException() {
        super("Buffer pool is fully exhausted");
    }
}
