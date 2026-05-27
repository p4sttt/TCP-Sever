package net.d4y2k.tcpsever.core;

public interface TcpServerAcceptor extends Runnable {
    void start() throws Exception;
    void stop();
    int getLocalPort();
}
