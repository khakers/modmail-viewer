@import com.github.khakers.modmailviewer.data.Attachment
@import com.github.khakers.modmailviewer.data.Message
@import com.github.khakers.modmailviewer.util.AttachmentFormatUtils

@param Attachment attachment
@param Message message
@param boolean nsfw
@param int galleryIndex = 0

<%--'data-type="video"' must be properly set, otherwise, some videos will download instead of opening the lightbox --%>

@if(attachment.filename().startsWith("SPOILER_"))
    <div class="image spoilerImage">
        <input id="${attachment.id()}" type="checkbox">
        @if(AttachmentFormatUtils.isImage(attachment))
            <a href="${attachment.url()}" class="glightbox" data-gallery="${"gallery"+galleryIndex}">
                <img class="image img-fluid rounded-2"
                     src="${attachment.url()}"
                     alt="Image ${attachment.filename()} uploaded by ${message.getAuthor().name()}"
                     loading="lazy">
            </a>
        @else
            <a href="${attachment.url()}" class="glightbox" data-type="video" data-gallery="${"gallery"+galleryIndex}">
                <video class="image img-fluid rounded-2"
                       src="${attachment.url()}"
                       preload="metadata"
                       controls>
                    Your browser does not support the video tag.
                </video>
            </a>
        @endif
        <label for="${attachment.id()}" class="spoilerImageButton">SPOILER</label>
    </div>
@elseif(nsfw)
    <div class="image spoilerImage">
        <input id="${attachment.id()}" type="checkbox">
        @if(AttachmentFormatUtils.isImage(attachment))
            <a href="${attachment.url()}" class="glightbox" data-gallery="${"gallery"+galleryIndex}">
                <img class="image img-fluid rounded-2"
                     src="${attachment.url()}"
                     alt="Image ${attachment.filename()} uploaded by ${message.getAuthor().name()}"
                     loading="lazy">
            </a>
        @else
            <a href="${attachment.url()}" class="glightbox" data-type="video" data-gallery="${"gallery"+galleryIndex}">
                <video class="image img-fluid rounded-2"
                       src="${attachment.url()}"
                       preload="metadata"
                       controls>
                    Your browser does not support the video tag.
                </video>
            </a>
        @endif
        <label for="${attachment.id()}" class="spoilerImageButton btn btn-outline-danger text-danger"><i
                    class="bi bi-exclamation-triangle-fill"></i> NSFW</label>
    </div>
@else
    <div class="image">
        @if(AttachmentFormatUtils.isImage(attachment))
            <a href="${attachment.url()}" class="glightbox" data-gallery="${"gallery"+galleryIndex}">
                <img class="image img-fluid rounded-2"
                     src="${attachment.url()}"
                     alt="Image ${attachment.filename()} uploaded by ${message.getAuthor().name()}"
                     loading="lazy">
            </a>
        @else
            <a href="${attachment.url()}" class="glightbox" data-type="video" data-gallery="${"gallery"+galleryIndex}">
                <%--                <span>potato</span>--%>
                <video class="image img-fluid rounded-2" controls src="${attachment.url()}" preload="metadata"></video>
            </a>
        @endif
    </div>

@endif

