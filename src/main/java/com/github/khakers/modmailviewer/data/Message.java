package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.util.DateFormatters;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Message implements Comparable<Message> {
    private final String id;

    private final Instant creationTime;
    private final String content;
    private final List<Attachment> attachments;
    private final User author;
    private final MessageType type;
    private final boolean isEdited;

//    public Message(
//            @JsonProperty("message_id")
//            String id,
//            @JsonProperty("timestamp")
//            String creationTime,
//            @JsonProperty("content")
//            String content,
//            @JsonProperty("attachments")
//            List<Attachment> attachments,
//            @JsonProperty("author")
//            User author,
//            @JsonProperty("type")
//            MessageType type,
//            @JsonProperty("edited")
//            boolean isEdited
//    ) {
//        this.id = id;
//        this.creationTime = DateFormatters.DATABASE_TIMESTAMP_FORMAT.parse(creationTime, Instant::from);
//        this.content = content;
//        this.attachments = attachments;
//        this.author = author;
//        this.type = type;
//        this.isEdited = isEdited;
//    }

    public Message(
            @JsonProperty("message_id") String id,
            @JsonProperty("timestamp")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME_STRING, timezone = "UTC")
            Instant creationTime,
            @JsonProperty("content") String content,
            @JsonProperty("attachments") List<Attachment> attachments,
            @JsonProperty("author") User author,
            @JsonProperty("type") MessageType type,
            @JsonProperty("edited") boolean isEdited) {
        this.id = id;
        this.creationTime = creationTime;
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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(@NotNull Message o) {
        if (this.creationTime.isBefore(o.creationTime))
            return 1;
        else if (this.creationTime.isAfter(o.creationTime))
            return -1;
        return 0;
    }
}
