package net.d4y2k.tcpsever.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.d4y2k.tcpsever.config.ConfigParser;
import net.d4y2k.tcpsever.config.ServerConfig;

public final class PropertiesConfigParser implements ConfigParser {
    @Override
    public ServerConfig parseConfig(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);

            int port = Integer.parseInt(properties.getProperty("port", "8080").trim());
            String host = properties.getProperty("host", "127.0.0.1").trim();
            int workersCount = Integer.parseInt(properties.getProperty("workersCount", "4").trim());
            int bufferSize = Integer.parseInt(properties.getProperty("bufferSize", "4096").trim());

            return new ServerConfigImpl(port, host, workersCount, bufferSize);
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Failed to read server configuration", e);
        }
    }
}
