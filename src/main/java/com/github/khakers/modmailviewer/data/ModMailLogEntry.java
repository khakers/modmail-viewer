package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.*;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ModMailLogEntry {
    @JsonProperty("_id")
    private final String _id;
    @JsonProperty("key")
    private final String key;

    @JsonProperty("open")
    private final boolean open;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS[XXX]", timezone = "UTC")
    @JsonProperty("created_at")
    private final Instant creationTime;

    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS[XXX]", timezone = "UTC")
    @JsonProperty("closed_at")
    private final Instant closedTime;

    @JsonProperty("bot_id")
    private final long botId;

    @JsonProperty("channel_id")
    private final long channelId;

    @JsonProperty("guild_id")
    private final long guildId;

    @JsonProperty("recipient")
    private final User recipient;

    @JsonProperty("creator")
    private final User creator;

    @Nullable
    @JsonProperty("closer")
    private final User closer;

    @Nullable
    @JsonProperty("close_message")
    private final String closeMessage;

    @JsonProperty("messages")
    private final List<Message> messages;

    @JsonProperty("nsfw")
    private final boolean nsfw;

    @Nullable
    @JsonProperty("title")
    private final String title;


    @JsonCreator
    public ModMailLogEntry(
            @JsonProperty("_id")
            String _id,
            @JsonProperty("key")
            String key,
            @JsonProperty("open")
            boolean open,
            @JsonProperty("created_at")
            Instant creationTime,
            @JsonProperty("closed_at")
            @Nullable
            Instant closedTime,
            @JsonProperty("bot_id")
            long botId,
            @JsonProperty("channel_id")
            long channelId,
            @JsonProperty("guild_id")
            long guildId,
            @JsonProperty("recipient")
            User recipient,
            @JsonProperty("creator")
            User creator,
            @JsonProperty("closer")
            @Nullable
            User closer,
            @JsonProperty("close_message")
            @Nullable
            String closeMessage,
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            @JsonProperty("messages")
            List<Message> messages,
            @JsonProperty("nsfw")
            boolean nsfw,
            @JsonProperty("title")
            @Nullable
            String title
    ) {
        this._id = key;
        this.key = key;
        this.open = open;
        this.creationTime = creationTime;
        this.closedTime = closedTime;
        this.botId = botId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.recipient = recipient;
        this.creator = creator;
        this.closer = closer;
        this.closeMessage = closeMessage;
        this.messages = messages;
        this.nsfw = nsfw;
        this.title = title;
    }


    public String getKey() {
        return key;
    }

    public boolean isOpen() {
        return open;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Optional<Instant> getClosedTime() {
        return Optional.ofNullable(closedTime);
    }

    public long getBotId() {
        return botId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public User getRecipient() {
        return recipient;
    }

    public User getCreator() {
        return creator;
    }

    public Optional<User> getCloser() {
        return Optional.ofNullable(closer);
    }

    public Optional<String> getCloseMessage() {
        return Optional.ofNullable(closeMessage);
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public List<Message> getMessages() {
        return messages;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public String get_id() {
        return _id;
    }

    @Override
    public String toString() {
        return "ModMailLogEntry{" +
                "_id='" + _id + '\'' +
                ", key='" + key + '\'' +
                ", open=" + open +
                ", creationTime=" + creationTime +
                ", closedTime=" + closedTime +
                ", botId=" + botId +
                ", channelId=" + channelId +
                ", guildId=" + guildId +
                ", recipient=" + recipient +
                ", creator=" + creator +
                ", closer=" + closer +
                ", closeMessage='" + closeMessage + '\'' +
                ", messages=" + messages +
                ", nsfw=" + nsfw +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModMailLogEntry that = (ModMailLogEntry) o;
        return open == that.open && botId == that.botId && channelId == that.channelId && guildId == that.guildId && nsfw == that.nsfw && Objects.equals(_id, that._id) && Objects.equals(key, that.key) && Objects.equals(creationTime, that.creationTime) && Objects.equals(closedTime, that.closedTime) && Objects.equals(recipient, that.recipient) && Objects.equals(creator, that.creator) && Objects.equals(closer, that.closer) && Objects.equals(closeMessage, that.closeMessage) && Objects.equals(messages, that.messages) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, key, open, creationTime, closedTime, botId, channelId, guildId, recipient, creator, closer, closeMessage, messages, nsfw, title);
    }
}
