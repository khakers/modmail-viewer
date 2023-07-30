@import com.github.khakers.modmailviewer.Main
@import com.github.khakers.modmailviewer.data.MessageType
@import com.github.khakers.modmailviewer.util.AttachmentFormatUtils
@import com.github.khakers.modmailviewer.util.DiscordUtils
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param com.github.khakers.modmailviewer.log.LogPage page
!{var parser = Main.PARSER;}
!{var renderer = Main.RENDERER;}
!{var modmailLog = page.log;}
!{var nf = java.text.NumberFormat.getInstance(getLocale());}

@template.layout.Page(page = page, content = @`
    <div class="container py-4">
        <div class="row">
            <h1>Modmail log ${modmailLog.getKey()}</h1>
            @if(modmailLog.getTitle().isPresent())
                <h2>${modmailLog.getTitle().get()}</h2>
            @endif
        </div>
        <div class="row">
            <div class="col">
                @if(modmailLog.isOpen())
                    <span class="badge rounded-pill text-bg-success fs-6"><i
                                class="bi bi-record-circle"></i> ${localize("LOG_STATUS_OPEN")}</span>
                @else
                    <span class="badge rounded-pill text-bg-danger fs-6"><i
                                class="bi bi-check-circle"></i> ${localize("LOG_STATUS_CLOSED")}</span>
                @endif
                @if(modmailLog.isNsfw())
                    <span class="badge rounded-pill text-bg-warning fs-6"><i
                                class="bi bi-exclamation-triangle"></i> ${localize("LOG_NSFW")}</span>
                @endif
                <span>Created by:</span>
                @template.macros.username(user = modmailLog.getCreator())
                <span>on <span class="text-muted text-nowrap"
                               data-timestamp="${modmailLog.getCreationTime().toString()}"
                               data-timestamp-type="detailed"></span></span>
                ·
                <span>${modmailLog.getMessages().size()} messages</span>

            </div>
        </div>
    </div>

    <div class="container">
        <ol class="list-group pb-5">
            @for(var message : gg.jte.support.ForSupport.of(modmailLog.getMessages()))
                !{String classes = null;}
                @if(!message.isLast() && message.get().canMergeMessages(modmailLog.getMessages().get(message.getIndex()+1)))
                    !{classes = "merge-below";}
                @endif
                <%-- Merged message --%>
                @if(!message.isFirst() && message.get().canMergeMessages(modmailLog.getMessages().get(message.getIndex()-1)))
                    <li class="list-group-item merge-above message ${classes}" id="${message.get().getId()}">
                        <div class="row">
                            <div class="col-2 col-md-1 text-center">
                                    <span class="d-none d-xl-inline fs-6 fs-light text-muted user-select-none"
                                          data-timestamp="${message.get().getCreationTime().toString()}"
                                          data-timestamp-type="basic" data-timestamp-title-type="detailed"
                                          data-bs-toggle="tooltip"></span>
                            </div>
                            <div class="col">
                                <div class="position-absolute message-actions -50 end-0 translate-middle-y me-3 z-1">
                                    @template.macros.MessageActionDropdown(message = message.get(), modmailLog = modmailLog)
                                </div>
                                @if(message.get().isEdited())
                                    <span class="fw-light float-end text-secondary">(${localize("MESSAGE_EDITED_FLAG")})  <i
                                                class="bi bi-pencil-fill ms-1"></i></span>
                                @endif
                                <div class="text-wrap text-break messageContent">
                                    $unsafe{renderer.render(parser.parse(message.get().getContent()))}
                                </div>
                                @if(!message.get().getAttachments().isEmpty())
                                    <div class="media-attachments mb-2">
                                        @for(var attachment: message.get().getAttachments().stream().filter(AttachmentFormatUtils::isMediaAttachment).toList())
                                            @template.macros.MediaAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw())
                                        @endfor
                                    </div>
                                    <div class="non-media-attachments mb-2">
                                        @for(var attachment: message.get().getAttachments().stream().filter(a -> !AttachmentFormatUtils.isMediaAttachment(a)).toList())
                                            @template.macros.FileAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw(), numberFormat = nf)
                                        @endfor
                                    </div>
                                @endif
                            </div>
                        </div>
                    </li>
                <%-- Unmergable message --%>
                @else
                    <li class="list-group-item message ${classes}" id="${message.get().getId()}">
                        <div class="row ">
                            <div class="col-2 col-md-1 text-center">
                                <div class="ration ratio-1x2">
                                <img width="50" src="${message.get().getAuthor().avatarUrl()}"
                                     data-avatar-id="${DiscordUtils.getAvatarId(message.get().getAuthor())}"
                                     class="rounded-circle discordAvatar" alt="user avatar">
                                </div>
                            </div>
                            <div class="col">
                                <div class="row ">
                                    <div class="col">
                                        <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${message.get().getAuthor().name()+DiscordUtils.getDiscriminatorString(message.get().getAuthor())}">${message.get().getAuthor().name()}</span>
                                            @if(message.get().getAuthor().mod())
                                                <span class="badge text-bg-primary align-middle fst-normal user-badge">
                                            <i class="bi bi-shield-shaded"></i>
                                            ${localize("USER_BADGE_MODERATOR")}
                                        </span>
                                            @endif
                                            @if(message.get().getType()==com.github.khakers.modmailviewer.data.MessageType.anonymous)
                                                <span class="badge text-bg-secondary align-middle fst-normal user-badge">
                                            <i class="bi bi-incognito"></i>
                                            ${localize("MESSAGE_TYPE_ANONYMOUS")}
                                        </span>
                                            @endif
                                            @if(message.get().getType()==MessageType.system)
                                                <span class="badge text-bg-info align-middle fst-normal user-badge">
                                            <i class="bi bi-gear-fill"></i>
                                            ${localize("MESSAGE_TYPE_SYSTEM")}
                                        </span>
                                            @endif
                                            @if(message.get().getType()==com.github.khakers.modmailviewer.data.MessageType.internal)
                                                <span class="badge text-bg-secondary align-middle fst-normal user-badge">
                                            <i class="bi bi-eye-slash"></i>
                                            ${localize("MESSAGE_TYPE_INTERNAL")}
                                        </span>
                                            @endif
                                            <span class="fs-6 text-muted" data-bs-toggle="tooltip"
                                                  data-timestamp="${message.get().getCreationTime().toString()}"
                                                  data-timestamp-type="relative"
                                                  data-timestamp-title-type="detailed"
                                            ></span>
                                        </h3>
                                        <div class="position-absolute message-actions end-0 top-0 mt-2 me-3 z-1">
                                            @template.macros.MessageActionDropdown(message = message.get(), modmailLog = modmailLog)
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col">
                                        <div class="text-wrap text-break messageContent">
                                            $unsafe{renderer.render(parser.parse(message.get().getContent()))}
                                        </div>
                                        @if(message.get().isEdited())
                                            <span class="fw-light">(${localize("MESSAGE_EDITED_FLAG")})</span>
                                        @endif
                                        @if(!message.get().getAttachments().isEmpty())
                                            <div class="media-attachments mb-2">
                                                @for(var attachment: message.get().getAttachments().stream().filter(AttachmentFormatUtils::isMediaAttachment).toList())
                                                    @template.macros.MediaAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw())
                                                @endfor
                                            </div>
                                            <div class="non-media-attachments mb-2">
                                                @for(var attachment: message.get().getAttachments().stream().filter(a -> !AttachmentFormatUtils.isMediaAttachment(a)).toList())
                                                    @template.macros.FileAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw(), numberFormat = nf)
                                                @endfor
                                            </div>
                                        @endif
                                    </div>
                                </div>
                            </div>
                        </div>
                    </li>
                @endif
            @endfor
            @if(!modmailLog.isOpen())
                <li class="list-group-item">
                    <div class="row ">
                        <div class="col-2 col-md-1 text-center">
                            <img width="50" src="${modmailLog.getCloser().get().avatarUrl()}"
                                 data-avatar-id="${DiscordUtils.getAvatarId(modmailLog.getCloser().get())}"
                                 class="rounded-circle discordAvatar" alt="user avatar">
                        </div>
                        <div class="col">
                            <div class="row ">
                                <div class="col">
                                    <h3>
                                        <span data-bs-toggle="tooltip"
                                              title="${modmailLog.getCloser().get().name()+DiscordUtils.getDiscriminatorString(modmailLog.getCloser().get())}">${modmailLog.getCloser().get().name()}</span>
                                        <span class="fw-normal fs-5">${localize("LOG_CLOSED_THREAD_TEXT")}</span>
                                        @if(modmailLog.getCloser().get().mod())
                                            <span class="badge text-bg-primary align-middle fst-normal user-badge">
                                            <i class="bi bi-shield-shaded"></i>
                                            ${localize("USER_BADGE_MODERATOR")}
                                        </span>
                                        @endif
                                        <span class="badge text-bg-info align-middle fst-normal user-badge">
                                            <i class="bi bi-gear-fill"></i>
                                            ${localize("MESSAGE_TYPE_SYSTEM")}
                                        </span>
                                        <span class="fs-6 text-muted" data-bs-toggle="tooltip"
                                              data-timestamp="${modmailLog.getClosedTime().get().toString()}"
                                              data-timestamp-type="relative"
                                              data-timestamp-title-type="detailed"
                                        >${modmailLog.getClosedTime().get().toString()}</span>
                                    </h3>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    @if(modmailLog.getCloseMessage().isPresent())
                                    <p>${modmailLog.getCloseMessage().get()}</p></div>
                                @endif
                            </div>
                        </div>
                    </div>
                </li>
            @endif
        </ol>
    </div>
`)

