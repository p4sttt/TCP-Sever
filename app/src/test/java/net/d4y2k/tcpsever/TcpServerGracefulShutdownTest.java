package net.d4y2k.tcpsever;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import net.d4y2k.tcpsever.config.impl.ServerConfigImpl;
import net.d4y2k.tcpsever.core.TcpServer;
import net.d4y2k.tcpsever.core.impl.TcpServerImpl;
import net.d4y2k.tcpsever.handler.impl.EchoChannelHandler;

public class TcpServerGracefulShutdownTest {

    @Test
    public void testGracefulShutdown() throws Exception {
        ServerConfigImpl config = new ServerConfigImpl(0, "127.0.0.1", 2, 1024);
        TcpServer server = new TcpServerImpl(config, new EchoChannelHandler());
        server.start();

        int port = server.getLocalPort();
        assertTrue(port > 0);

        // Connect client 1
        Socket clientSocket1 = new Socket("127.0.0.1", port);
        PrintWriter out1 = new PrintWriter(clientSocket1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));

        // Send a message to queue writes
        String message = "Flush me before you stop!";
        out1.println(message);

        // Stop the server *while* client 1 is connected
        server.stop();

        // Verify that client 1 still receives its echoed message (flushed successfully)
        String response = in1.readLine();
        assertEquals(message, response, "Graceful shutdown should flush pending writes to client 1");
        
        clientSocket1.close();

        // Attempting to connect a new client after server has been stopped must fail
        assertThrows(IOException.class, () -> {
            new Socket("127.0.0.1", port);
        }, "Should not accept new client connections after stopping");
    }
}
