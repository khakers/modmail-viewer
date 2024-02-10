@import java.util.List;
@import com.github.khakers.modmailviewer.data.Attachment
@import com.github.khakers.modmailviewer.data.Message
@import com.github.khakers.modmailviewer.util.AttachmentFormatUtils;

@param List<Attachment> attachments
@param gg.jte.support.ForSupport<Message> message
@param boolean nsfw = false
@param java.text.NumberFormat numberFormat = java.text.NumberFormat.getInstance()


@if(attachments != null && !attachments.isEmpty())
    <div class="media-attachments mb-2 row" data-gallery="${"gallery"+message.getIndex()}">
        @for(var attachment: attachments.stream().filter(AttachmentFormatUtils::isMediaAttachment).filter(attachment -> attachment.getAttachmentURI().isPresent()).toList())
            <div class="col-md-auto col-12">
                @template.macros.MediaAttachment(attachment = attachment, message = message.get(), nsfw = nsfw, galleryIndex = message.getIndex())
            </div>
        @endfor
    </div>
    <div class="non-media-attachments mb-2">
        @for(var attachment: attachments.stream().filter(a -> !AttachmentFormatUtils.isMediaAttachment(a)).toList())
            @template.macros.FileAttachment(attachment = attachment, message = message.get(), nsfw = nsfw, numberFormat = numberFormat)
        @endfor
    </div>
@endif