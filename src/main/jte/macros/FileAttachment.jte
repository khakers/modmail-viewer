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
        !{var uri = attachment.getAttachmentURI().orElse(null);}
        <h5 class="card-title"><a href="${uri}">${attachment.filename()}</a></h5>
        @if(attachment.getAttachmentURI().isEmpty())
            <i class="bi bi-exclamation-triangle"></i>
            <span class="text-body-secondary">Attachment not found</span>
        @endif
        <h6 class="card-subtitle mb-2 text-body-secondary">${attachment.size()} Bytes</h6>
        @if(AttachmentFormatUtils.isAudio(attachment))
            <audio controls src="${uri}" preload="metadata"></audio>
        @elseif(AttachmentFormatUtils.isVideo(attachment))
            <video controls src="${uri}" preload="metadata"></video>
        @endif
    </div>
</div>