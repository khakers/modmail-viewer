@import com.github.khakers.modmailviewer.auth.SiteUser
@import com.github.khakers.modmailviewer.data.MessageType
@import io.javalin.http.Context


@param Context ctx


@param com.github.khakers.modmailviewer.data.ModMailLogEntry modmailLog
@param SiteUser user
@param com.vladsch.flexmark.parser.Parser parser
@param com.vladsch.flexmark.html.HtmlRenderer renderer

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html" >
<head>
    <title>Modmail Log ${modmailLog.getTitle().orElse(modmailLog.getKey())}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <%--    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet"--%>
    <%--          integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">--%>
    <%--    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css"--%>
    <%--          integrity="sha384-xeJqLiuOvjUBq3iGOjvSQSIlwrpqjSHXpduPd6rQpuiM3f5/ijby8pCsnbu5S81n" crossorigin="anonymous">--%>
    <%--    <link rel="stylesheet"--%>
    <%--          href="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.6.0/build/styles/default.min.css"--%>
    <%--          integrity="sha256-+94KwJIdhsNWxBUy5zGciHojvRuP8ABgyrRHJJ8Dx88=" crossorigin="anonymous">--%>

    <link href="/webjars/bootstrap/5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/webjars/bootstrap-icons/1.10.2/font/bootstrap-icons.css">
    <link rel="stylesheet" href="/webjars/highlightjs__cdn-assets/11.6.0/styles/a11y-dark.min.css">
<%--    <link rel="stylesheet" href="/webjars/highlightjs__cdn-assets/11.6.0/styles/default.min.css">--%>
    <link rel="stylesheet" href="/css/styles.css">

</head>
<body>

<div class="page-container">
    @template.macros.navbar(ctx = ctx, user = user)
    @if(modmailLog.isNsfw())
        <div id="nsfw-backdrop" style=" background-color: rgba(255,255,255,0)"></div>
    @endif
    <div class="content">
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
                        <span class="badge rounded-pill text-bg-danger fs-6"><i
                                    class="bi bi-check-circle"></i> Closed</span>
                    @endif
                    @if(modmailLog.isNsfw())
                        <span class="badge rounded-pill text-bg-warning fs-6"><i
                                    class="bi bi-exclamation-triangle"></i> NFSW</span>
                    @endif
                    <span>Created by:</span>
                    <span class="fw-semibold">${modmailLog.getCreator().name()}</span><span
                            class="text-secondary">#${modmailLog.getCreator().discriminator()}</span>
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
                        <li class="list-group-item merge-above ${classes}">
                            <div class="row">
                                <div class="col-2 col-md-1 text-center">
                            <span class="d-none d-xl-inline fs-6 fs-light text-muted user-select-none"
                                  data-timestamp="${message.get().getCreationTime().toString()}"
                                  data-timestamp-type="basic" data-timestamp-title-type="detailed"
                                  data-bs-toggle="tooltip"></span>
                                </div>
                                <div class="col">
                                    <%--                            <p class="mb-0"></p>--%>
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
                        </li>
                    <%-- Unmergable message --%>
                    @else
                        <li class="list-group-item ${classes}">
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
</div>

<script src="/timeformat.js"></script>

<%--<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"--%>
<%--        integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"--%>
<%--        crossorigin="anonymous"></script>--%>
<%--<script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@11.6.0/build/highlight.min.js"--%>
<%--        integrity="sha256-4v2jQZxK6PbZEeZ2xl2ziov6NHMksBFgBlxtMZVYbQk=" crossorigin="anonymous"></script>--%>
<script src="/webjars/bootstrap/5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
<script src="/webjars/highlightjs__cdn-assets/11.6.0/highlight.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/twemoji@14.0.2/dist/twemoji.min.js"
        integrity="sha256-cGIk2NxUQEYPjtkcGmqtJdcyr24O5vsxFRsVerSFurs=" crossorigin="anonymous"></script>
@if(modmailLog.isNsfw())
    <div class="modal" id="nsfwModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1"
         aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="staticBackdropLabel"><i
                                class="bi bi-exclamation-triangle-fill text-warning fs-3"></i> NSFW Thread</h1>
                </div>
                <div class="modal-body">
                    <p>This Thread has been marked as containing Not Safe For Work content.</p>
                    <p>Do you wish to continue?</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="history.back()">Go Back</button>
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal">Continue</button>
                </div>
            </div>
        </div>
    </div>
    <script>
        const modalElement = document.getElementById('nsfwModal');
        const bsModal = new bootstrap.Modal(modalElement)
        modalElement.addEventListener('hide.bs.modal', event => {
            document.getElementById("nsfw-backdrop").hidden = true;
        })
        bsModal.show()
    </script>
@endif
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
    hljs.highlightAll();
</script>
<script>
    twemoji.parse(document.body, {
        // The default Twemoji CDN will die at the end of the year. This tells it to use jsdelivr for emoji images instead
        base: "https://cdn.jsdelivr.net/gh/twitter/twemoji@v14.0.2/assets/"
    });
</script>
<script>
    let spoilerTags = document.querySelectorAll('.spoilerText');
    spoilerTags.forEach((item) => {
        item.addEventListener("click", (event) => {
            let target = event.target;
            target.classList.remove("hidden");
            target.setAttribute("role", "presentation")
        })
    })
</script>
<script src="/js/ThemeSelector.js"></script>
</body>
</html>