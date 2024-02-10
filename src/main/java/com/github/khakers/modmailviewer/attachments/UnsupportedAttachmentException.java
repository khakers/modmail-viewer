package com.github.khakers.modmailviewer.attachments;

/**
 * The attachment was found, but does not contain all the data required to properly return it.
 */
public class UnsupportedAttachmentException extends Exception{
    public UnsupportedAttachmentException(String message) {
        super(message);
    }
}
