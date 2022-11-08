package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.util.DateFormatters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Message {
    private final String id;
    private final Instant creationTime;
    private final String content;
    private final List<Attachment> attachments;
    private final User author;
    private final MessageType type;
    private final boolean isEdited;

    private static final Logger logger = LogManager.getLogger();

    public Message(
            @JsonProperty("message_id")
            String id,
            @JsonProperty("timestamp")
            String creationTime,
            @JsonProperty("content")
            String content,
            @JsonProperty("attachments")
            List<Attachment> attachments,
            @JsonProperty("author")
            User author,
            @JsonProperty("type")
            MessageType type,
            @JsonProperty("edited")
            boolean isEdited
    ) {
        this.id = id;
        this.creationTime = DateFormatters.DATABASE_TIMESTAMP_FORMAT.parse(creationTime, Instant::from);
        this.content = content;
        this.attachments = attachments;
        this.author = author;
        this.type = type;
        this.isEdited = isEdited;
    }

    /**
     * Returns true if the given message and this message can be merged
     * Messages are merged if the Authors are the same, and they were sent less than 7 minutes apart
     *
     * @param message Previous message to evaluate for mergeability
     * @return True if the messages can be merged
     */
    public boolean canMergeMessages(Message message) {
        return message.getAuthor().equals(this.author)
                && this.type == message.getType()
                && (Math.abs(Duration.between(message.getCreationTime(), this.creationTime).getSeconds()) < 60 * 7);
    }

    public String getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public String getContent() {
        return content;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public User getAuthor() {
        return author;
    }

    public MessageType getType() {
        return type;
    }

    @JsonProperty("edited")
    public boolean isEdited() {
        return isEdited;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Message) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.creationTime, that.creationTime) &&
                Objects.equals(this.content, that.content) &&
                Objects.equals(this.attachments, that.attachments) &&
                Objects.equals(this.author, that.author) &&
                Objects.equals(this.type, that.type) &&
                this.isEdited == that.isEdited;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationTime, content, attachments, author, type, isEdited);
    }

    @Override
    public String toString() {
        return "Message[" +
                "id=" + id + ", " +
                "creationTime=" + creationTime + ", " +
                "content=" + content + ", " +
                "attachments=" + attachments + ", " +
                "author=" + author + ", " +
                "type=" + type + ", " +
                "isEdited=" + isEdited + ']';
    }

}
