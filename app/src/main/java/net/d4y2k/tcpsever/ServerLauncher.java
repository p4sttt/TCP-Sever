package net.d4y2k.tcpsever;

import java.io.InputStream;
import net.d4y2k.tcpsever.config.ServerConfig;
import net.d4y2k.tcpsever.config.impl.PropertiesConfigParser;
import net.d4y2k.tcpsever.config.impl.ServerConfigImpl;
import net.d4y2k.tcpsever.core.TcpServer;
import net.d4y2k.tcpsever.core.impl.TcpServerImpl;
import net.d4y2k.tcpsever.handler.impl.EchoChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLauncher {
    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting TCP server...");

            InputStream configStream = ServerLauncher.class.getClassLoader().getResourceAsStream("server.properties");
            ServerConfig config;

            if (configStream != null) {
                config = new PropertiesConfigParser().parseConfig(configStream);
                logger.info("Loaded configurations from server.properties");
            } else {
                logger.warn("server.properties not found in resources. Fallback to default properties.");
                config = new ServerConfigImpl(8080, "127.0.0.1", 4, 4096);
            }

            TcpServer server = new TcpServerImpl(config, new EchoChannelHandler());
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutdown hook running, shutting down server...");
                    server.stop();
                } catch (Exception e) {
                    logger.error("Error during graceful shutdown", e);
                }
            }, "ShutdownHookThread"));

        } catch (Exception e) {
            logger.error("Failed to start the TCP server", e);
            System.exit(1);
        }
    }
}
