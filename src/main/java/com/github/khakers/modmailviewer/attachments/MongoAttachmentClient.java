package com.github.khakers.modmailviewer.attachments;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;

public class MongoAttachmentClient implements AttachmentClient {
    protected MongoDatabase mongoDatabase;
    protected JacksonMongoCollection<MongoAttachment> collection;

    public MongoAttachmentClient(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        // I wanted to use the mongodb java driver's pojo support, but it doesn't support integer types being null...
        // So I'm using mongojack instead
        this.collection = JacksonMongoCollection.builder().build(mongoDatabase, "attachments", MongoAttachment.class, UuidRepresentation.STANDARD);
    }

    @Override
    public AttachmentResult getAttachment(long id) throws AttachmentNotFoundException {
        var result = collection.find(Filters.eq("_id", id)).first();
        if (result == null) {
            throw new AttachmentNotFoundException("attachment of id " + id + " not found");
        }

        return new AttachmentResult(result.data(), result.contentType());
    }
}
