package com.github.khakers.modmailviewer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class SingleItemCache<T> {
    private static final Logger logger = LogManager.getLogger();

    /**
     * The cached item
     */
    private T item;

    /**
     * The last time the item was updated, stored as a system.nanotime value
     */
    private long lastUpdated;

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
     * Returns true if the elapsed time since the last update is greater than the cache expiration length
     *
     * @return True if the cache is expired
     */
    private boolean isExpired() {
        return cacheExpirationLength * 1000000 < System.nanoTime() - lastUpdated;
    }

    /**
     * Get the cached item, refreshing it using the getValueFunc if it is expired
     *
     * @return The cached item
     * @throws Exception If the item could not be retrieved
     */
    public synchronized T getItem() throws Exception {
        logger.traceEntry();
        if (this.item == null || lastUpdated == 0 || isExpired()) {
            this.item = getValueFunc.call();
            lastUpdated = System.nanoTime();
            logger.trace("grabbed new {} from {}", item.getClass().toString(), lastUpdated);
            logger.traceExit(this.item);
            return this.item;
        }
        logger.trace("grabbed cached {} from {}", item.getClass().toString(), lastUpdated);
        logger.traceExit(item);
        return item;
    }

    /**
     * Manually set the cache value.
     * <p>
     * This will update the last updated time to the current time.
     *
     * @param item The item to set the cache value to
     */
    public synchronized void setItem(T item) {
        this.item = item;
        lastUpdated = System.nanoTime();
        logger.trace("manually set {}", item.getClass().toString());

    }
}

