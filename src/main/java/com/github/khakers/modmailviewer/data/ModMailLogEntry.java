package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.khakers.modmailviewer.util.DateFormatters.DATABASE_TIMESTAMP_FORMAT;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ModMailLogEntry {
    private final String key;
    private final boolean open;
    private final Instant creationTime;
    @Nullable
    private final Instant closedTime;
    private final long botId;
    private final long channelId;
    private final long guildId;
    private final User recipient;
    private final User creator;
    @Nullable
    private final User closer;
    private final Optional<String> closeMessage;
    private final List<Message> messages;
    private final boolean nsfw;
    private final Optional<String> title;

    //    @BsonCreator
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ModMailLogEntry(
            @JsonProperty("key")
            String key,
            @JsonProperty("open")
            boolean open,
            @JsonProperty("created_at")
            String creationTime,
            @JsonProperty("closed_at")
            @Nullable
            String closedTime,
            @JsonProperty("bot_id")
            String botId,
            @JsonProperty("channel_id")
            String channelId,
            @JsonProperty("guild_id")
            String guildId,
            @JsonProperty("recipient")
            User recipient,
            @JsonProperty("creator")
            User creator,
            @JsonProperty("closer")
            @Nullable
            User closer,
            @JsonProperty("close_message")
            String closeMessage,
            @JsonProperty("message")
            List<Message> messages,
            @JsonProperty("nsfw")
            Boolean nsfw,
            @JsonProperty("title")
            String title
    ) {
        this.key = key;
        this.open = open;
        this.creationTime = DATABASE_TIMESTAMP_FORMAT.parse(creationTime, Instant::from);
        if (closedTime != null) {
            this.closedTime = DATABASE_TIMESTAMP_FORMAT.parse(closedTime, Instant::from);
        } else {
            this.closedTime = null;
        }
        this.botId = Long.parseLong(botId);
        this.channelId = Long.parseLong(channelId);
        this.guildId = Long.parseLong(guildId);
        this.recipient = recipient;
        this.creator = creator;
        this.closer = closer;
        this.closeMessage = Optional.ofNullable(closeMessage);
        this.messages = messages;
        this.nsfw = Objects.requireNonNullElse(nsfw, false);
        this.title = Optional.ofNullable(title);
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
        return closeMessage;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public Optional<String> getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "ModMailLogEntry{" +
                "key='" + key + '\'' +
                ", open=" + open +
                ", creationTime=" + creationTime +
                ", closedTime=" + closedTime +
                ", botId=" + botId +
                ", channelId=" + channelId +
                ", guildId=" + guildId +
                ", recipient=" + recipient +
                ", creator=" + creator +
                ", closer=" + closer +
                ", closeMessage=" + closeMessage +
                ", messages=" + messages +
                ", nsfw=" + nsfw +
                ", title=" + title +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModMailLogEntry that = (ModMailLogEntry) o;
        return open == that.open && botId == that.botId && channelId == that.channelId && guildId == that.guildId && nsfw == that.nsfw && key.equals(that.key) && creationTime.equals(that.creationTime) && closedTime.equals(that.closedTime) && recipient.equals(that.recipient) && creator.equals(that.creator) && closer.equals(that.closer) && closeMessage.equals(that.closeMessage) && messages.equals(that.messages) && title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, open, creationTime, closedTime, botId, channelId, guildId, recipient, creator, closer, closeMessage, messages, nsfw, title);
    }

}
