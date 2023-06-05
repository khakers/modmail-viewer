@import com.github.khakers.modmailviewer.auth.UserToken
@import com.github.khakers.modmailviewer.data.MessageType
@import com.github.khakers.modmailviewer.util.DiscordUtils
@import io.javalin.http.Context


@param Context ctx


@param com.github.khakers.modmailviewer.data.ModMailLogEntry modmailLog
@param UserToken user
@param com.vladsch.flexmark.parser.Parser parser
@param com.vladsch.flexmark.html.HtmlRenderer renderer

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Log ${modmailLog.getTitle().orElse(modmailLog.getKey())}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    @template.macros.HeaderImports()
</head>
<body>

    @template.macros.navbar(ctx = ctx, user = user)
    <div class="content" up-main data-nsfw="${modmailLog.isNsfw()}">
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
                                    class="bi bi-record-circle"></i> Open</span>
                    @else
                        <span class="badge rounded-pill text-bg-danger fs-6"><i
                                    class="bi bi-check-circle"></i> Closed</span>
                    @endif
                    @if(modmailLog.isNsfw())
                        <span class="badge rounded-pill text-bg-warning fs-6"><i
                                    class="bi bi-exclamation-triangle"></i> NFSW</span>
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
                        <li class="list-group-item merge-above ${classes}" id="${message.get().getId()}">
                            <div class="row">
                                <div class="col-2 col-md-1 text-center">
                            <span class="d-none d-xl-inline fs-6 fs-light text-muted user-select-none"
                                  data-timestamp="${message.get().getCreationTime().toString()}"
                                  data-timestamp-type="basic" data-timestamp-title-type="detailed"
                                  data-bs-toggle="tooltip"></span>
                                </div>
                                <div class="col">
                                    @if(message.get().isEdited())
                                        <span class="fw-light float-end text-secondary">(edited)  <i class="bi bi-pencil-fill ms-1"></i></span>
                                    @endif
                                    <div class="text-wrap text-break messageContent">
                                        $unsafe{renderer.render(parser.parse(message.get().getContent()))}
                                    </div>
                                    @if(!message.get().getAttachments().isEmpty())
                                        @for(var attachment: message.get().getAttachments())
                                            @if(attachment.isImage())
                                                @template.macros.imageAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw())
                                            @endif
                                        @endfor
                                    @endif
                                </div>
                            </div>
                        </li>
                    <%-- Unmergable message --%>
                    @else
                        <li class="list-group-item ${classes}" id="${message.get().getId()}">
                            <div class="row ">
                                <div class="col-2 col-md-1 text-center">
                                    <img width="50" src="${message.get().getAuthor().avatarUrl()}"
                                         onerror="this.src='https://cdn.discordapp.com/embed/avatars/${DiscordUtils.getAvatarId(message.get().getAuthor())}.png';this.onerror=null"
                                         class="rounded-circle" style="height: inherit;" alt="user avatar">
                                </div>
                                <div class="col">
                                    <div class="row ">
                                        <div class="col">
                                            <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${message.get().getAuthor().name()+DiscordUtils.getDiscriminatorString(message.get().getAuthor())}">${message.get().getAuthor().name()}</span>
                                                @if(message.get().getAuthor().mod())
                                                    <span class="badge text-bg-primary align-middle fst-normal"
                                                          style="font-size: 12px;">
                                            <i class="bi bi-shield-shaded"></i>
                                            Staff
                                        </span>
                                                @endif
                                                @if(message.get().getType()==com.github.khakers.modmailviewer.data.MessageType.anonymous)
                                                    <span class="badge text-bg-secondary align-middle fst-normal"
                                                          style="font-size: 12px;">
                                            <i class="bi bi-incognito"></i>
                                            Anonymous
                                        </span>
                                                @endif
                                                @if(message.get().getType()==MessageType.system)
                                                    <span class="badge text-bg-info align-middle fst-normal"
                                                          style="font-size: 12px;">
                                            <i class="bi bi-gear-fill"></i>
                                            System
                                        </span>
                                                @endif
                                                @if(message.get().getType()==com.github.khakers.modmailviewer.data.MessageType.internal)
                                                    <span class="badge text-bg-secondary align-middle fst-normal"
                                                          style="font-size: 12px;">
                                            <i class="bi bi-eye-slash"></i>
                                            Internal
                                        </span>
                                                @endif
                                                <span class="fs-6 text-muted" data-bs-toggle="tooltip"
                                                      data-timestamp="${message.get().getCreationTime().toString()}"
                                                      data-timestamp-type="relative"
                                                      data-timestamp-title-type="detailed"
                                                ></span>
                                            </h3>
                                        </div>
                                        <%--                            <div class="col d-flex justify-content-end">--%>
                                        <%--                                <button type="button" class="btn btn-light" href="https://discord.com/channels/655478055497760778/715242843609759852/1032710569070956667"><i class="bi bi-box-arrow-up-right"></i></button>--%>
                                        <%--                            </div>--%>
                                    </div>
                                    <div class="row">
                                        <div class="col">
                                            <%--                                    <p class="mb-0"></p>--%>
                                            <div class="text-wrap text-break messageContent">
                                                $unsafe{renderer.render(parser.parse(message.get().getContent()))}
                                            </div>
                                            @if(message.get().isEdited())
                                                <span class="fw-light">(edited)</span>
                                            @endif
                                            @if(!message.get().getAttachments().isEmpty())
                                                @for(var attachment: message.get().getAttachments())
                                                    @if(attachment.isImage())
                                                        @template.macros.imageAttachment(attachment = attachment, message = message.get(), nsfw = modmailLog.isNsfw())
                                                    @endif
                                                @endfor
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
                                     onerror="this.src='https://cdn.discordapp.com/embed/avatars/${DiscordUtils.getAvatarId(modmailLog.getCloser().get())}.png';this.onerror=null"
                                     class="rounded-circle" style="height: inherit;" alt="user avatar">
                            </div>
                            <div class="col">
                                <div class="row ">
                                    <div class="col">
                                        <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${modmailLog.getCloser().get().name()+DiscordUtils.getDiscriminatorString(modmailLog.getCloser().get())}">${modmailLog.getCloser().get().name()}</span>
                                            <span class="fw-normal fs-5">closed the thread</span>
                                            @if(modmailLog.getCloser().get().mod())
                                                <span class="badge text-bg-primary align-middle fst-normal"
                                                      style="font-size: 12px;">
                                            <i class="bi bi-shield-shaded"></i>
                                            Staff
                                        </span>
                                            @endif
                                            <span class="badge text-bg-info align-middle fst-normal"
                                                  style="font-size: 12px;">
                                        <i class="bi bi-gear-fill"></i>
                                        System
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
    </div>
    @template.macros.footer()
</body>
</html>
