/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 The Play Remote Configuration Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.playrconf.provider;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import io.playrconf.sdk.AbstractProvider;
import io.playrconf.sdk.FileCfgObject;
import io.playrconf.sdk.KeyValueCfgObject;
import io.playrconf.sdk.exception.ProviderException;
import io.playrconf.sdk.exception.RemoteConfException;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Retrieves configuration from Redis.
 *
 * @author Thibault Meyer
 * @since 18.04.02
 */
public class RedisProvider extends AbstractProvider {

    /**
     * Contains the provider version.
     */
    private static String providerVersion;

    @Override
    public String getName() {
        return "Redis";
    }

    @Override
    public String getVersion() {
        if (RedisProvider.providerVersion == null) {
            synchronized (RedisProvider.class) {
                final Properties properties = new Properties();
                final InputStream is = RedisProvider.class.getClassLoader()
                    .getResourceAsStream("playrconf-redis.properties");
                try {
                    properties.load(is);
                    RedisProvider.providerVersion = properties.getProperty("playrconf.redis.version", "unknown");
                    properties.clear();
                    is.close();
                } catch (final IOException ignore) {
                }
            }
        }
        return RedisProvider.providerVersion;
    }

    @Override
    public String getConfigurationObjectName() {
        return "redis";
    }

    @Override
    public void loadData(final Config config,
                         final Consumer<KeyValueCfgObject> kvObjConsumer,
                         final Consumer<FileCfgObject> fileObjConsumer) throws ConfigException, RemoteConfException {
        final String redisHost = config.getString("host");
        final int redisPort = config.getInt("port");
        final int redisDb = config.hasPath("db") ? config.getInt("db") : 0;
        final String redisPassword = config.hasPath("password") ? config.getString("password") : "";
        final String prefix = config.hasPath("prefix") ? config.getString("prefix") : "";
        final String separator = config.hasPath("separator") ? config.getString("separator") : ".";

        // Check values
        if (redisHost == null || redisHost.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "host", "Required");
        } else if (redisPort <= 0 || redisPort > 65535) {
            throw new ConfigException.BadValue(config.origin(), "port", "Invalid port number");
        } else if (redisDb < 0) {
            throw new ConfigException.BadValue(config.origin(), "db", "Must be greater or equal to 0");
        } else if (separator.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "separator", "Required");
        }

        // Connect to Redis
        try (final Jedis jedis = new Jedis(redisHost, redisPort)) {

            // Connect
            jedis.connect();
            if (!jedis.isConnected()) {
                throw new ProviderException("Can't connect to the provider");
            }

            // Authentication
            if (!redisPassword.isEmpty()) {
                jedis.auth(redisPassword);
            }

            // Select the right db
            if (redisDb > 0) {
                jedis.select(redisDb);
            }

            // Iterate over matching prefix keys
            jedis.keys(prefix + "*").forEach(key -> {

                // Retrieve value
                final String cfgValue = jedis.get(key);

                // Clean key
                String cfgKey = key
                    .replaceFirst(prefix, "")
                    .replace(separator, ".");
                if (!cfgKey.matches("[0-9a-zA-Z_](.*)")) {
                    cfgKey = cfgKey.substring(1);
                }

                // Check if current configuration object is a file
                if (isFile(cfgValue)) {
                    fileObjConsumer.accept(
                        new FileCfgObject(cfgKey, cfgValue)
                    );
                } else {

                    // Standard configuration value
                    kvObjConsumer.accept(
                        new KeyValueCfgObject(cfgKey, cfgValue)
                    );
                }
            });
        }
    }
}
