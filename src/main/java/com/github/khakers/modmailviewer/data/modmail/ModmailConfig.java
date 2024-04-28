package com.github.khakers.modmailviewer.data.modmail;

import com.fasterxml.jackson.annotation.*;
import com.github.khakers.modmailviewer.auth.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties({"_id"})
public class ModmailConfig {
    @JsonIgnore
    Map<String, Object> unknownFields = new HashMap<>();
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
    @JsonIgnore
    private Map<Long, Role> flatUserPermMap;

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

    @JsonAnyGetter
    public Map<String, Object> getUnknownFields() {
        return unknownFields;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        unknownFields.put(key, value);
    }


    @JsonIgnore
    public Map<Long, Role> getFlatUserPerms() {
        if (flatUserPermMap != null) {
            return flatUserPermMap;
        }
        Map<Long, Role> roleMap = new HashMap<>();
        levelPermissions.forEach((role, longs) -> longs.forEach(id -> roleMap.put(id, role)));
        flatUserPermMap = roleMap;
        return roleMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModmailConfig that = (ModmailConfig) o;
        return botID == that.botID && logChannelId == that.logChannelId && mainCategoryId == that.mainCategoryId && recipientThreadClose == that.recipientThreadClose && Objects.equals(unknownFields, that.unknownFields) && Objects.equals(closeEmoji, that.closeEmoji) && Objects.equals(levelPermissions, that.levelPermissions) && Objects.equals(flatUserPermMap, that.flatUserPermMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unknownFields, botID, closeEmoji, levelPermissions, logChannelId, mainCategoryId, recipientThreadClose, flatUserPermMap);
    }

    @Override
    public String toString() {
        return "ModmailConfig{" +
              "unknownFields=" + unknownFields +
              ", botID=" + botID +
              ", closeEmoji='" + closeEmoji + '\'' +
              ", levelPermissions=" + levelPermissions +
              ", logChannelId=" + logChannelId +
              ", mainCategoryId=" + mainCategoryId +
              ", recipientThreadClose=" + recipientThreadClose +
              ", flatUserPermMap=" + flatUserPermMap +
              '}';
    }
}
