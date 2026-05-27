package net.d4y2k.tcpsever.core;

public interface WorkerPool {
    void startAll();
    void shutDownAll();
    EventLoopWorker getNextWorker();
}
