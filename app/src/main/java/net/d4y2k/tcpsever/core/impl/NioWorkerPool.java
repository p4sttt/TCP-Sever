package net.d4y2k.tcpsever.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.d4y2k.tcpsever.core.DirectBufferPool;
import net.d4y2k.tcpsever.core.EventLoopWorker;
import net.d4y2k.tcpsever.core.WorkerPool;
import net.d4y2k.tcpsever.handler.ChannelHandler;

public final class NioWorkerPool implements WorkerPool {
    private static final Logger logger = LoggerFactory.getLogger(NioWorkerPool.class);

    private final EventLoopWorker[] workers;
    private final Thread[] threads;
    private final DirectBufferPool bufferPool;
    private int nextWorkerIndex = 0;

    public NioWorkerPool(int poolSize, int bufferSize, ChannelHandler handler) {
        this.workers = new EventLoopWorker[poolSize];
        this.threads = new Thread[poolSize];
        this.bufferPool = new NioDirectBufferPool(poolSize, bufferSize);

        for (int i = 0; i < poolSize; ++i) {
            try {
                this.workers[i] = new NioEventLoopWorker(bufferSize, bufferPool, handler);
                this.threads[i] = new Thread(this.workers[i], "WorkerThread-" + i);
            } catch (Exception e) {
                logger.error("Failed to initialize NioEventLoopWorker at index {}", i, e);
            }
        }
    }

    @Override
    public synchronized EventLoopWorker getNextWorker() {
        EventLoopWorker worker = workers[nextWorkerIndex];
        nextWorkerIndex = (nextWorkerIndex + 1) % workers.length;
        return worker;
    }

    @Override
    public void startAll() {
        for (Thread thread : threads) {
            if (thread != null) {
                thread.start();
            }
        }
        logger.info("NioWorkerPool started with {} threads", workers.length);
    }

    @Override
    public void shutDownAll() {
        logger.info("Shutting down all worker threads gracefully...");
        for (EventLoopWorker worker : workers) {
            if (worker != null) {
                worker.stopGracefully();
            }
        }
        for (Thread thread : threads) {
            if (thread != null) {
                try {
                    thread.join(4000); // Wait up to 4s for graceful drainage and exit
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("NioWorkerPool shutdown complete");
    }
}
