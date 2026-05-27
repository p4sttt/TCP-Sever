package net.d4y2k.tcpsever.core.impl;

import net.d4y2k.tcpsever.core.EventLoopWorker;
import net.d4y2k.tcpsever.core.WorkerBalancer;
import net.d4y2k.tcpsever.core.WorkerPool;

public final class RoundRobinWorkerBalancer implements WorkerBalancer {
    private final WorkerPool workerPool;

    public RoundRobinWorkerBalancer(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    @Override
    public EventLoopWorker balance() {
        return workerPool.getNextWorker();
    }
}
