package net.d4y2k.tcpsever.config;

public interface ServerConfig {
    int getPort();
    String getHost();
    int getWorkersCount();
    int getBufferSize();
}
