package net.d4y2k.tcpsever.config;

import java.io.InputStream;

public interface ConfigParser {
    ServerConfig parseConfig(InputStream inputStream);
}
