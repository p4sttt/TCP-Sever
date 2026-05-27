package net.d4y2k.tcpsever.config.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import net.d4y2k.tcpsever.config.ConfigParser;
import net.d4y2k.tcpsever.config.ServerConfig;

public class ConfigParserTest {

    @Test
    public void testConfigParser() {
        String propertiesContent = "port=9090\nhost=192.168.1.1\nworkersCount=8\nbufferSize=2048\n";
        InputStream inputStream = new ByteArrayInputStream(propertiesContent.getBytes());

        ConfigParser parser = new PropertiesConfigParser();
        ServerConfig config = parser.parseConfig(inputStream);

        assertNotNull(config);
        assertEquals(9090, config.getPort());
        assertEquals("192.168.1.1", config.getHost());
        assertEquals(8, config.getWorkersCount());
        assertEquals(2048, config.getBufferSize());
    }

    @Test
    public void testConfigParserDefaults() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        ConfigParser parser = new PropertiesConfigParser();
        ServerConfig config = parser.parseConfig(inputStream);

        assertNotNull(config);
        assertEquals(8080, config.getPort());
        assertEquals("127.0.0.1", config.getHost());
        assertEquals(4, config.getWorkersCount());
        assertEquals(4096, config.getBufferSize());
    }
}
