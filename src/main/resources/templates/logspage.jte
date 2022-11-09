@import com.github.khakers.modmailviewer.auth.SiteUser
@import com.github.khakers.modmailviewer.data.MessageType

@param com.github.khakers.modmailviewer.data.ModMailLogEntry modmailLog
@param SiteUser user

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Logs</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css">
</head>
<body style="padding-top: 56px;">

@template.navbar(user = user)

<div class="container py-4">
    <div class="row">
        <h1>Modmail log ${modmailLog.getKey()}</h1>
    </div>
    <div class="row">
        <div class="col">
            @if(modmailLog.isOpen())
                <span class="badge rounded-pill text-bg-success fs-6"><i
                            class="bi bi-record-circle"></i> Open</span>
            @else
                <span class="badge rounded-pill text-bg-danger fs-6"><i class="bi bi-x-circle"></i> Closed</span>
            @endif
            @if(modmailLog.isNsfw())
                <span class="badge rounded-pill text-bg-danger fs-6"><i
                            class="bi bi-exclamation-triangle"></i> NFSW</span>
            @endif
            <span>Created by:</span>
            <span class="fw-semibold">${modmailLog.getCreator().name()}</span><span
                    class="text-secondary">#${modmailLog.getCreator().discriminator()}</span>
            <span>on <span class="text-muted" timestamp="${modmailLog.getCreationTime().toString()}"
                           timestamp-type="detailed"></span></span>
            ·
            <span>${modmailLog.getMessages().size()} messages</span>

        </div>
    </div>
</div>

<div class="container">
    <ol class="list-group pb-5">
        @for(var message : gg.jte.support.ForSupport.of(modmailLog.getMessages()))
            !{String style = null;}
            !{String classes = null;}
            @if(!message.isLast() && message.get().canMergeMessages(modmailLog.getMessages().get(message.getIndex()+1)))
                !{style = "border-bottom-style: hidden;";}
                !{classes = "mb-0 pb-0";}
            @endif
            <%-- Merged message --%>
            @if(!message.isFirst() && message.get().canMergeMessages(modmailLog.getMessages().get(message.getIndex()-1)))
                <li class="list-group-item mt-0 pt-0 ${classes}" style="border-top-style: hidden; ${style}">
                    <div class="row">
                        <div class="col-2 col-md-1 text-center">
                            <span class="fs-6 fs-light text-muted user-select-none"
                                  timestamp="${message.get().getCreationTime().toString()}"
                                  timestamp-type="basic" timestamp-title-type="detailed"
                                  data-bs-toggle="tooltip"></span>
                        </div>
                        <div class="col">
                            <p class="mb-0">${message.get().getContent()}</p>
                            @if(!message.get().getAttachments().isEmpty())
                                @for(var attachment: message.get().getAttachments())
                                    @if(attachment.isImage())
                                        <img class="img-fluid rounded-2" src="${attachment.url()}"
                                             alt="Image ${attachment.filename()} uploaded by ${message.get().getAuthor().name()}">

                                    @endif
                                @endfor
                            @endif
                        </div>
                    </div>
                </li>
            <%-- Unmergable message --%>
            @else
                <li class="list-group-item ${classes}"
                    style="${style}">
                    <div class="row ">
                        <div class="col-2 col-md-1 text-center">
                            <img width="50" src="${message.get().getAuthor().avatarUrl()}"
                                 onerror="this.src='https://cdn.discordapp.com/embed/avatars/${Integer.parseInt(message.get().getAuthor().discriminator()) % 5}.png';this.onerror=null"
                                 class="rounded-circle" style="height: inherit;" alt="user avatar">
                        </div>
                        <div class="col">
                            <div class="row ">
                                <div class="col">
                                    <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${message.get().getAuthor().name()+"#"+message.get().getAuthor().discriminator()}">${message.get().getAuthor().name()}</span>
                                        @if(message.get().getAuthor().mod())
                                            <span class="badge text-bg-primary align-middle fst-normal"
                                                  style="font-size: 12px;">
                                            <i class="bi bi-shield-shaded"></i>
                                            Moderator
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
                                              timestamp="${message.get().getCreationTime().toString()}"
                                              timestamp-type="relative" timestamp-title-type="detailed"
                                        ></span>
                                    </h3>
                                </div>
                                <%--                            <div class="col d-flex justify-content-end">--%>
                                <%--                                <button type="button" class="btn btn-light" href="https://discord.com/channels/655478055497760778/715242843609759852/1032710569070956667"><i class="bi bi-box-arrow-up-right"></i></button>--%>
                                <%--                            </div>--%>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <p class="mb-0">${message.get().getContent()}</p>
                                    @if(message.get().isEdited())
                                        <span class="fw-light">(edited)</span>
                                    @endif
                                    @if(!message.get().getAttachments().isEmpty())
                                        @for(var attachment: message.get().getAttachments())
                                            @if(attachment.isImage())
                                                <img class="img-fluid rounded-2" src="${attachment.url()}"
                                                     alt="Image ${attachment.filename()} uploaded by ${message.get().getAuthor().name()}">
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
                             onerror="this.src='https://cdn.discordapp.com/embed/avatars/${Integer.parseInt(modmailLog.getCloser().get().discriminator()) % 5}.png';this.onerror=null"
                             class="rounded-circle" style="height: inherit;" alt="user avatar">
                    </div>
                    <div class="col">
                        <div class="row ">
                            <div class="col">
                                <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${modmailLog.getCloser().get().name()+"#"+modmailLog.getCloser().get().discriminator()}">${modmailLog.getCloser().get().name()}</span>
                                    <span class="fw-normal fs-5">closed the thread</span>
                                    @if(modmailLog.getCloser().get().mod())
                                        <span class="badge text-bg-primary align-middle fst-normal"
                                              style="font-size: 12px;">
                                            <i class="bi bi-shield-shaded"></i>
                                            Moderator
                                        </span>
                                    @endif
                                    <span class="badge text-bg-info align-middle fst-normal"
                                          style="font-size: 12px;">
                                        <i class="bi bi-gear-fill"></i>
                                        System
                                    </span>

                                    <span class="fs-6 text-muted" data-bs-toggle="tooltip"
                                          timestamp="${modmailLog.getClosedTime().get().toString()}"
                                          timestamp-type="relative"
                                          timestamp-title-type="detailed"
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

<script src="/timeformat.js"></script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"
        crossorigin="anonymous"></script>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>