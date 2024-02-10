package com.github.khakers.modmailviewer.page.attachment;


import com.github.khakers.modmailviewer.attachments.AttachmentClient;
import com.github.khakers.modmailviewer.attachments.AttachmentNotFoundException;
import com.github.khakers.modmailviewer.attachments.UnsupportedAttachmentException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttachmentController {
    private static final Logger logger = LogManager.getLogger();

    protected AttachmentClient handler;

    public AttachmentController(AttachmentClient handler) {
        this.handler = handler;
    }

    public void handle (Context ctx) {
        logger.traceEntry();
        var id = ctx.pathParamAsClass("id", Long.class).get();
        logger.debug("getting attachment id {}", id);
        try {
            var attachment_data = handler.getAttachment(id);

            ctx.contentType(attachment_data.content_type());
            ctx.result(attachment_data.attachmentData());
            // set caching headers
            ctx.header("max-age", "604800").header("immutable","");
            // Override X-Frame-Options header
            // This is required for the attachment viewer to work
            ctx.header("X-Frame-Options", "SAMEORIGIN");
        } catch (AttachmentNotFoundException e) {
            throw new NotFoundResponse();
        } catch (UnsupportedAttachmentException e) {
            logger.throwing(e);
            throw new InternalServerErrorResponse("");
        }
        logger.traceExit();

    }
    public Handler getHandler() {
        return this::handle;
    }
}
