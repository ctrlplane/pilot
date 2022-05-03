package io.ctrlplane.pilot.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** The kek cache configuration. */
@Configuration
public class CacheConfig {

    /** The logger for this class. */
    public static final Logger LOG =
            LoggerFactory.getLogger(CacheConfig.class);

    /** The timeout for the cache. */
    @Value("${keyRequest.cache.timeout}")
    private int cacheTimeout;

    /** The timeout units for the cache. */
    @Value("${keyRequest.cache.units}")
    private TimeUnit cacheTimeoutUnits;

    /**
     * Creates the caffeine configuration bean.
     *
     * @return The caffeine configuration.
     */
    @Bean
    public Caffeine<?,?> caffeineConfig() {
        return Caffeine.newBuilder()
                .scheduler(Scheduler.forScheduledExecutorService(
                        Executors.newScheduledThreadPool(1)))
                .evictionListener(
                        (k, v, cause) -> LOG.debug(
                                "Removing cached value for {}, {}", k, cause))
                .expireAfterAccess(this.cacheTimeout, this.cacheTimeoutUnits);
    }

}
