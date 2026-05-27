package net.d4y2k.tcpsever.config.impl;

import net.d4y2k.tcpsever.config.ServerConfig;

public final class ServerConfigImpl implements ServerConfig {
    private final int port;
    private final String host;
    private final int workersCount;
    private final int bufferSize;

    public ServerConfigImpl(int port, String host, int workersCount, int bufferSize) {
        this.port = port;
        this.host = host;
        this.workersCount = workersCount;
        this.bufferSize = bufferSize;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getWorkersCount() {
        return workersCount;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }
}
