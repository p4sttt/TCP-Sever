package net.d4y2k.tcpsever;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.junit.jupiter.api.Test;
import net.d4y2k.tcpsever.config.impl.ServerConfigImpl;
import net.d4y2k.tcpsever.core.TcpServer;
import net.d4y2k.tcpsever.core.impl.TcpServerImpl;
import net.d4y2k.tcpsever.handler.impl.EchoChannelHandler;

public class TcpServerIntegrationTest {

    @Test
    public void testEchoServer() throws Exception {
        ServerConfigImpl config = new ServerConfigImpl(0, "127.0.0.1", 2, 1024);
        TcpServer server = new TcpServerImpl(config, new EchoChannelHandler());
        server.start();

        int port = server.getLocalPort();
        assertTrue(port > 0, "Server port should be assigned > 0");

        try (Socket clientSocket = new Socket("127.0.0.1", port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String message = "Hello Async Decoupled Server!";
            out.println(message);

            String response = in.readLine();
            assertEquals(message, response, "Server should echo the message exactly");

        } finally {
            server.stop();
        }
    }
}
