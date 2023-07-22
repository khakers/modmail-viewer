@import com.github.khakers.modmailviewer.data.Message
@import com.github.khakers.modmailviewer.data.ModMailLogEntry
@import com.github.khakers.modmailviewer.util.DiscordUtils

@import java.util.Optional
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param Message message
@param ModMailLogEntry modmailLog

!{Optional<String> messageLink = DiscordUtils.getMessageURI(message, modmailLog);}

<div class="dropdown dropstart">
    <button type="button" class="btn btn-sm btn-secondary dropdown-toggle shadow-lg message-actions-button"
            data-bs-toggle="dropdown" aria-expanded="false"><i class="bi bi-three-dots"></i></button>
    <ul class="dropdown-menu">
        <li>
            @if(messageLink.isEmpty())
            <%-- https://getbootstrap.com/docs/5.3/components/tooltips/#disabled-elements --%>
                <span class="" tabindex="0" data-bs-toggle="tooltip"
                      data-bs-title="${localize("MESSAGE_OPTIONS_DISCORD_LINK_DISABLED_EXPLANATION")}">
                    <a class="dropdown-item disabled" disabled>${localize("MESSAGE_OPTIONS_COPY_DISCORD_LINK")}</a>
                    </span>
            @else
                <button class="dropdown-item message-action-button"
                        data-copy-string="${messageLink.get()}">${localize("MESSAGE_OPTIONS_COPY_DISCORD_LINK")}
                    </button>
            @endif
        </li>
        <li>
            <button class="dropdown-item message-action-button"
                    data-copy-type="uri-fragment"
                    data-copy-string="#${message.getId()}">${localize("MESSAGE_OPTIONS_COPY_VIEWER_LINK")}</button>
        </li>
        <li>
            <hr class="dropdown-divider">
        </li>
        <li>
            <button class="dropdown-item message-action-button"
                    data-copy-string="${message.getId()}">${localize("MESSAGE_OPTIONS_COPY_MESSAGE_ID")}</button>
        </li>
        <li>
            <button class="dropdown-item message-action-button"
                    data-copy-string="${message.getAuthor().id()}">${localize("MESSAGE_OPTIONS_COPY_AUTHOR_ID")}</button>
        </li>
    </ul>
</div>

