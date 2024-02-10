package com.github.khakers.modmailviewer.attachments;

public interface AttachmentClient {
    AttachmentResult getAttachment(long id) throws AttachmentNotFoundException, UnsupportedAttachmentException;
}
