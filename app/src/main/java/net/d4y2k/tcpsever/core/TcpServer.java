package net.d4y2k.tcpsever.core;

public interface TcpServer {
    void start() throws Exception;
    void stop() throws Exception;
    int getLocalPort();
}
