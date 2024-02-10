package com.github.khakers.modmailviewer.attachments;

public record AttachmentResult(
      byte[] attachmentData,
      String content_type

) {
}
