package net.d4y2k.tcpsever.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import net.d4y2k.tcpsever.core.EventLoopWorker;
import net.d4y2k.tcpsever.core.WorkerBalancer;
import net.d4y2k.tcpsever.core.WorkerPool;
import net.d4y2k.tcpsever.handler.ChannelContext;
import net.d4y2k.tcpsever.handler.ChannelHandler;

public class WorkerBalancerTest {

    @Test
    public void testWorkerBalancerRoundRobin() throws Exception {
        ChannelHandler dummyHandler = new ChannelHandler() {
            @Override
            public void onConnect(ChannelContext ctx) {}
            @Override
            public void onData(ChannelContext ctx, byte[] data, int length) {}
            @Override
            public void onDisconnect(ChannelContext ctx) {}
        };

        int workersCount = 3;
        int bufferSize = 1024;
        WorkerPool pool = new NioWorkerPool(workersCount, bufferSize, dummyHandler);
        WorkerBalancer balancer = new RoundRobinWorkerBalancer(pool);

        EventLoopWorker w1 = balancer.balance();
        EventLoopWorker w2 = balancer.balance();
        EventLoopWorker w3 = balancer.balance();
        EventLoopWorker w4 = balancer.balance();

        assertNotNull(w1);
        assertNotNull(w2);
        assertNotNull(w3);

        assertEquals(w1, w4);
    }
}
