package com.github.khakers.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

public enum MessageType {
    @BsonProperty(value = "thread_message")
    @BsonRepresentation(BsonType.STRING)
    @JsonProperty("thread_message")
    thread,
    internal,
    anonymous
}
