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
import com.typesafe.config.ConfigFactory;
import io.playrconf.sdk.FileCfgObject;
import io.playrconf.sdk.Provider;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * RedisProviderTest.
 *
 * @author Thibault Meyer
 * @since 18.04.02
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisProviderTest {

    /**
     * Retrieves initial configuration.
     *
     * @param prefix The prefix to use with the provider
     * @return The initial configuration
     */
    private Config getInitialConfiguration(final String prefix) {
        return ConfigFactory.parseString(
            "redis.host = \"terra.zero-x-baadf00d.com\"\n"
                + "redis.port = 6379\n"
                + "redis.db = 0\n"
                + "redis.prefix = \"" + prefix + "\"\n"
                + "redis.separator = \".\"\n"
        );
    }

    @Test
    public void redisTest_001() {
        // Load remote configuration
        final StringBuilder stringBuilder = new StringBuilder(512);
        final Provider provider = new RedisProvider();
        final Config initialConfig = this.getInitialConfiguration("test");
        provider.loadData(
            initialConfig.getConfig(provider.getConfigurationObjectName()),
            keyValueCfgObject -> keyValueCfgObject.apply(stringBuilder),
            FileCfgObject::apply
        );
        final Config remoteConfig = ConfigFactory
            .parseString(stringBuilder.toString())
            .withFallback(initialConfig);

        // Test version
        final Properties properties = new Properties();
        final InputStream is = RedisProvider.class.getClassLoader()
            .getResourceAsStream("playrconf-redis.properties");
        try {
            properties.load(is);
            Assert.assertEquals(
                provider.getVersion(),
                properties.getProperty("playrconf.redis.version", "unknown")
            );
            properties.clear();
            is.close();
        } catch (final IOException ignore) {
        }

        // Standard values
        Assert.assertEquals(
            "org.postgresql.Driver",
            remoteConfig.getString("db.default.driver")
        );
        Assert.assertEquals(
            5000,
            remoteConfig.getInt("db.default.timeout")
        );
        Assert.assertEquals(
            Arrays.asList(1, 2, 3, 4, 5),
            remoteConfig.getIntList("db.default.excludedIds")
        );
        Assert.assertFalse(remoteConfig.getBoolean("db.default.disabled"));
    }

    @Test
    public void redisTest_002() {
        // Load remote configuration
        final StringBuilder stringBuilder = new StringBuilder(512);
        final Provider provider = new RedisProvider();
        final Config initialConfig = this.getInitialConfiguration("");
        provider.loadData(
            initialConfig.getConfig(provider.getConfigurationObjectName()),
            keyValueCfgObject -> keyValueCfgObject.apply(stringBuilder),
            FileCfgObject::apply
        );
        final Config remoteConfig = ConfigFactory
            .parseString(stringBuilder.toString())
            .withFallback(initialConfig);

        // Standard values
        Assert.assertEquals(
            "Hello World",
            remoteConfig.getString("my.key")
        );

        Assert.assertEquals(
            Arrays.asList(1, 2, 3, 4, 5),
            remoteConfig.getIntList("test.db.default.excludedIds")
        );

        // File
        final File file = new File("./test");
        try {
            final InputStream initialStream = new FileInputStream(file);
            final byte[] buffer = new byte[128];
            final int nbRead = initialStream.read(buffer);
            buffer[nbRead] = '\0';
            Assert.assertTrue(nbRead > 0);
            Assert.assertEquals(
                "Hello World!",
                new String(buffer, 0, nbRead)
            );
        } catch (final IOException ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
