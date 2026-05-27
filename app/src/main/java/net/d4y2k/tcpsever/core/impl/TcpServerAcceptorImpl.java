package net.d4y2k.tcpsever.core.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.d4y2k.tcpsever.config.ServerConfig;
import net.d4y2k.tcpsever.core.EventLoopWorker;
import net.d4y2k.tcpsever.core.TcpServerAcceptor;
import net.d4y2k.tcpsever.core.WorkerBalancer;

public final class TcpServerAcceptorImpl implements TcpServerAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerAcceptorImpl.class);

    private final ServerConfig config;
    private final WorkerBalancer balancer;
    
    private ServerSocketChannel serverChannel;
    private Thread thread;
    private volatile boolean running = true;

    public TcpServerAcceptorImpl(ServerConfig config, WorkerBalancer balancer) {
        this.config = config;
        this.balancer = balancer;
    }

    @Override
    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()));
        serverChannel.configureBlocking(true);

        thread = new Thread(this, "AcceptorThread");
        thread.start();
        logger.info("TcpServerAcceptorImpl started on {}:{}", config.getHost(), config.getPort());
    }

    @Override
    public void run() {
        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                logger.info("Accepted connection from {}", clientChannel.getRemoteAddress());

                EventLoopWorker worker = balancer.balance();
                if (worker != null) {
                    worker.registerNewClient(clientChannel);
                } else {
                    logger.warn("No worker available, closing connection immediately");
                    clientChannel.close();
                }
            } catch (IOException e) {
                if (!running) {
                    break;
                }
                logger.error("Error accepting connection", e);
            }
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            logger.error("Failed to close ServerSocketChannel", e);
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("TcpServerAcceptorImpl stopped");
    }

    @Override
    public int getLocalPort() {
        if (serverChannel != null && serverChannel.isOpen()) {
            try {
                return ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
            } catch (IOException e) {
                return -1;
            }
        }
        return -1;
    }
}
