package net.d4y2k.tcpsever.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.d4y2k.tcpsever.config.ServerConfig;
import net.d4y2k.tcpsever.core.TcpServer;
import net.d4y2k.tcpsever.core.TcpServerAcceptor;
import net.d4y2k.tcpsever.core.WorkerBalancer;
import net.d4y2k.tcpsever.core.WorkerPool;
import net.d4y2k.tcpsever.handler.ChannelHandler;

public final class TcpServerImpl implements TcpServer {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerImpl.class);

    private final ServerConfig config;
    private final ChannelHandler handler;
    
    private WorkerPool workerPool;
    private WorkerBalancer balancer;
    private TcpServerAcceptor acceptor;

    public TcpServerImpl(ServerConfig config, ChannelHandler handler) {
        this.config = config;
        this.handler = handler;
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting TcpServerImpl...");
        
        workerPool = new NioWorkerPool(config.getWorkersCount(), config.getBufferSize(), handler);
        workerPool.startAll();

        balancer = new RoundRobinWorkerBalancer(workerPool);

        acceptor = new TcpServerAcceptorImpl(config, balancer);
        acceptor.start();

        logger.info("TcpServerImpl started successfully");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping TcpServerImpl...");
        if (acceptor != null) {
            acceptor.stop();
        }
        if (workerPool != null) {
            workerPool.shutDownAll();
        }
        logger.info("TcpServerImpl stopped");
    }

    @Override
    public int getLocalPort() {
        return acceptor != null ? acceptor.getLocalPort() : -1;
    }
}
