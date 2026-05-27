package net.d4y2k.tcpsever.core;

public interface WorkerBalancer {
    EventLoopWorker balance();
}
