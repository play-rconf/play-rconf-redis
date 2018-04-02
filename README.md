# Play Remote Configuration - Redis


[![Latest release](https://img.shields.io/badge/latest_release-18.04-orange.svg)](https://github.com/play-rconf/play-rconf-redis/releases)
[![JitPack](https://jitpack.io/v/play-rconf/play-rconf-redis.svg)](https://jitpack.io/#play-rconf/play-rconf-redis)
[![Build](https://img.shields.io/travis-ci/play-rconf/play-rconf-redis.svg?branch=master&style=flat)](https://travis-ci.org/play-rconf/play-rconf-redis)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/play-rconf/play-rconf-redis/master/LICENSE)

Retrieves configuration from Redis
*****

## About this project
In production, it is not always easy to manage the configuration files of a
Play Framework application, especially when it running on multiple servers.
The purpose of this project is to provide a simple way to use a remote
configuration with a Play Framework application.



## How to use

To enable this provider, just add the classpath `"io.playrconf.provider.RedisProvider"`
and the following configuration:

```hocon
remote-configuration {

  ## Redis
  # ~~~~~
  # Retrieves configuration from Redis
  redis {

    # Redis host. Must be an IP address or a valid hostname
    host = "127.0.0.1"
    host = ${?REMOTECONF_REDIS_HOST}

    # Defines the port on which the server is listening. By
    # default, Redis server listen on 6379
    port = 6379
    port = ${?REMOTECONF_REDIS_PORT}

    # Defines the database to use. Must be a valid number.
    # Check your Redis configuration to know the hightest
    # value you are able to use
    db = 0
    db = ${?REMOTECONF_REDIS_DB}

    # If password authentication is anabled on
    # your Redis server, this variable allow you
    # to set the password to use at the connection
    password = ""
    password = ${?REMOTECONF_REDIS_PASSWORD}

    # Prefix. Get only values with key beginning
    # with the configured prefix
    prefix = ""
    prefix = ${?REMOTECONF_REDIS_PREFIX}

    # Which pattern you have used for keys segmentation.
    # ie: "my.key.cfg", the separator is "." (dot).
    # ie: "my/key/cfg", the separator is "/" (slash).
    separator = "."
    separator = ${?REMOTECONF_REDIS_SEPARATOR}
  }
}
```



## License
This project is released under terms of the [MIT license](https://raw.githubusercontent.com/play-rconf/play-rconf-redis/master/LICENSE).
