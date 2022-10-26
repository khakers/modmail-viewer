package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.auth.Role;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties({"_id"})
public class ModmailConfig {

    @JsonProperty("bot_id")
    private long botID;

    @JsonProperty("close_emoji")
    private String closeEmoji;

    @JsonProperty("level_permissions")
    private Map<Role, List<Long>> levelPermissions;
    @JsonProperty("log_channel_id")
    private long logChannelId;

    @JsonProperty("main_category_id")
    private long mainCategoryId;

    @JsonProperty("recipient_thread_close")
    private int recipientThreadClose;

    public long getBotID() {
        return botID;
    }

    public String getCloseEmoji() {
        return closeEmoji;
    }

    public Map<Role, List<Long>> getLevelPermissions() {
        return levelPermissions;
    }

    public long getLogChannelId() {
        return logChannelId;
    }

    public long getMainCategoryId() {
        return mainCategoryId;
    }

    public int getRecipientThreadClose() {
        return recipientThreadClose;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModmailConfig that = (ModmailConfig) o;
        return botID == that.botID && logChannelId == that.logChannelId && mainCategoryId == that.mainCategoryId && recipientThreadClose == that.recipientThreadClose && closeEmoji.equals(that.closeEmoji) && levelPermissions.equals(that.levelPermissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botID, closeEmoji, levelPermissions, logChannelId, mainCategoryId, recipientThreadClose);
    }

    @Override
    public String toString() {
        return "ModmailConfig{" + "botID=" + botID + ", closeEmoji='" + closeEmoji + '\'' + ", levelPermissions=" + levelPermissions + ", logChannelId=" + logChannelId + ", mainCategoryId=" + mainCategoryId + ", recipientThreadClose=" + recipientThreadClose + '}';
    }
}
