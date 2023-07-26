package com.github.khakers.modmailviewer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.Callable;

public class SingleItemCache<T> {
    private static final Logger logger = LogManager.getLogger();

    /**
     * The cached item
     */
    private T item;

    /**
     * The last time the item was updated
     */
    private Instant lastUpdated;

    /**
     * Time in milliseconds to cache the item for
     */
    private long cacheExpirationLength = 1000;

    /**
     * Function used to get the cached item when it needs to be refreshed
     */
    private Callable<T> getValueFunc;

    public SingleItemCache(long cacheDurationMS) {
        this.cacheExpirationLength = cacheDurationMS;
    }

    /**
     * A cache that only stores a single item and synchronizes access to it
     *
     * @param cacheDurationMS How long to cache the item for
     * @param getValueFunc   Function use to get the cached item when it needs to be refreshed
     */
    public SingleItemCache(long cacheDurationMS, Callable<T> getValueFunc) {
        this.cacheExpirationLength = cacheDurationMS;
        this.getValueFunc = getValueFunc;
    }

    /**
     * Get the cached item, refreshing it using the getValueFunc if it is expired
     *
     * @return The cached item
     * @throws Exception If the item could not be retrieved
     */
    public synchronized T getItem() throws Exception {
        if (this.item == null || lastUpdated == null || Instant.now().isAfter(lastUpdated.plusMillis(cacheExpirationLength))) {
            this.item = getValueFunc.call();
            lastUpdated = Instant.now();
            logger.debug("grabbed new {} from {}", item.getClass().toString(), lastUpdated.toString());
            return this.item;
        }
        logger.debug("grabbed cached {} from {}", item.getClass().toString(), lastUpdated.toString());
        return item;
    }

    public synchronized void setItem(T item) {
        this.item = item;
        lastUpdated = Instant.now();
        logger.debug("manually set {}", item.getClass().toString());

    }
}

