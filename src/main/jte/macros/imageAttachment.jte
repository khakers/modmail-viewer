@import com.github.khakers.modmailviewer.data.Attachment
@import com.github.khakers.modmailviewer.data.Message

@param Attachment attachment
@param Message message

@if(attachment.filename().startsWith("SPOILER_"))
    <div class="image spoilerImage">
        <input id="${attachment.id()}" type="checkbox">
        <img class="image img-fluid rounded-2"
             src="${attachment.url()}"
             alt="Image ${attachment.filename()} uploaded by ${message.getAuthor().name()}"
             loading="lazy">
        <label for="${attachment.id()}" class="spoilerImageButton">SPOILER</label>
    </div>
@else
    <div class="image">
        <img class="image img-fluid rounded-2"
             src="${attachment.url()}"
             alt="Image ${attachment.filename()} uploaded by ${message.getAuthor().name()}"
             loading="lazy">
    </div>
@endif

