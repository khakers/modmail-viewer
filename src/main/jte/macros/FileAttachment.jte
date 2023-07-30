@import com.github.khakers.modmailviewer.data.Attachment
@import com.github.khakers.modmailviewer.data.Message
@import com.github.khakers.modmailviewer.util.AttachmentFormatUtils
@import java.text.NumberFormat

@param Attachment attachment
@param Message message
@param boolean nsfw
@param NumberFormat numberFormat


<div class="card attachment-card">
    <div class="card-body">
        <h5 class="card-title"><a href="${attachment.url()}">${attachment.filename()}</a></h5>
        <h6 class="card-subtitle mb-2 text-body-secondary">${attachment.size()}</h6>
        @if(AttachmentFormatUtils.isAudio(attachment))
            <audio controls src="${attachment.url()}" preload="metadata"></audio>
        @elseif(AttachmentFormatUtils.isVideo(attachment))
            <video controls src="${attachment.url()}" preload="metadata"></video>
        @endif
    </div>
</div>