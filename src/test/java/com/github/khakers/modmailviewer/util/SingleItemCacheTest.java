package com.github.khakers.modmailviewer.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

class SingleItemCacheTest {

    SingleItemCache<Integer> cache;

    @BeforeEach
    void setUp() {
        Callable<Integer> cacheGetFunc = () -> 1;
        cache = new SingleItemCache<>(50, cacheGetFunc);
    }

    // Ensures that the cache updates when the cache is empty
    @Test
    void getItemUpdatesWhenCacheEmpty() throws Exception {
        Assertions.assertEquals(1, cache.getItem());
    }

    // Ensures that the cache updates after the cache duration has passed
    @Test
    void getItemUpdatesAfterCacheDuration() throws Exception {
        cache.setItem(0);
        //TODO find an alternative to this
        Thread.sleep(70);
        Assertions.assertEquals(1, cache.getItem());
    }

    // Ensures that the cache does not update early
    @Test
    void getItemDoesNotUpdateEarly() throws Exception {
        cache.setItem(0);
        Thread.sleep(20);
        Assertions.assertEquals(0, cache.getItem());
    }
}