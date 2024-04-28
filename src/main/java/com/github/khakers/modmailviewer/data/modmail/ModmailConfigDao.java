package com.github.khakers.modmailviewer.data.modmail;

import java.util.Optional;

public interface ModmailConfigDao {
    /**
     * Gets the config for the bot with the id given in the constructor
     *
     * @return The config for the bot
     */
    ModmailConfig getConfig();

    /**
     * Retrieve a single value from the config with the given key
     *
     * @param key The key to get the value of
     * @return The value of the key
     */
    Optional<Object> getSingleConfigValue(String key);

    /**
     * Update a value in the config of the given key
     *
     * @param key The key to update
     * @param value The value to update the key to
     * @param <V> The type of the value
     */
    <V> void updateConfigValue(String key, V value);
}
