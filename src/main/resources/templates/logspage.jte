@param com.github.khakers.data.ModMailLogEntry modmailLog

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
<body>

<div class="container py-4">
    <div class="row">
        <h1>Modmail log ${modmailLog.getKey()}</h1>
    </div>
    <div class="row">
        <div class="col">
            @if(modmailLog.isOpen())
                <span class="badge rounded-pill text-bg-success fs-6">Open</span>
            @else
                <span class="badge rounded-pill text-bg-danger fs-6"><i class="bi bi-x-circle"></i> Closed</span>
            @endif
            <span>Created by:</span>
            <span>${modmailLog.getCreator().name()}</span><span class="text-muted">#${modmailLog.getCreator().discriminator()}</span>
            <span>on ${modmailLog.getCreationTime().toString()}</span>
            ·
            <span>${modmailLog.getMessages().size()} messages</span>

        </div>
    </div>
</div>

<div class="container">
    <ol class="list-group">
        @for(com.github.khakers.data.Message message : modmailLog.getMessages())
            <li class="list-group-item">
                <%--                <div class="container">--%>
                <div class="row ">
                    <div class="col-1 text-center">
                        <img width="50" src="${message.author().avatarUrl()}"
                             onerror="this.src='https://cdn.discordapp.com/embed/avatars/${Integer.parseInt(message.author().discriminator()) % 5}.png'"
                             class="rounded-circle" style="height: inherit;" alt="user avatar">
                    </div>
                    <div class="col">
                        <div class="row ">
                            <div class="col">
                                <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${message.author().name()+"#"+message.author().discriminator()}">${message.author().name()}</span>
                                    @if(message.author().mod())
                                        <span class="badge rounded-pill text-bg-primary align-middle fst-normal"
                                              style="font-size: 12px;">
                                            <i class="bi bi-shield"></i>
                                            Moderator
                                        </span>
                                    @endif
                                    @if(message.type()==com.github.khakers.data.MessageType.anonymous)
                                        <span class="badge rounded-pill text-bg-secondary align-middle fst-normal"
                                              style="font-size: 12px;">
                                            <i class="bi bi-incognito"></i>
                                            Anonymous
                                        </span>
                                    @endif
                                    @if(message.type()==com.github.khakers.data.MessageType.internal)
                                        <span class="badge rounded-pill text-bg-secondary align-middle fst-normal"
                                              style="font-size: 12px;">
                                            <i class="bi bi-eye-slash"></i>
                                            Internal
                                        </span>
                                    @endif
                                    <span class="fs-6" data-bs-toggle="tooltip"
                                          title="${message.creationTime()}">${message.creationTime()}</span>
                                </h3>
                            </div>
                            <%--                            <div class="col d-flex justify-content-end">--%>
                            <%--                                <button type="button" class="btn btn-light" href="https://discord.com/channels/655478055497760778/715242843609759852/1032710569070956667"><i class="bi bi-box-arrow-up-right"></i></button>--%>
                            <%--                            </div>--%>
                        </div>
                        <div class="row">
                            <div class="col">
                                <p>${message.content()}</p></div>
                        </div>
                    </div>
                </div>
            </li>
        @endfor
        @if(!modmailLog.isOpen())
            <li class="list-group-item">
                <%--                <div class="container">--%>
                <div class="row ">
                    <div class="col-1 text-center">
                        <img width="50" src="${modmailLog.getCloser().avatarUrl()}"
                             onerror="this.src='https://cdn.discordapp.com/embed/avatars/${Integer.parseInt(modmailLog.getCloser().discriminator()) % 5}.png'"
                             class="rounded-circle" style="height: inherit;" alt="user avatar">
                    </div>
                    <div class="col">
                        <div class="row ">
                            <div class="col">
                                <h3>
                                    <span data-bs-toggle="tooltip"
                                          title="${modmailLog.getCloser().name()+"#"+modmailLog.getCloser().discriminator()}">${modmailLog.getCloser().name()}</span>
                                    <span class="fw-normal fs-5">closed the thread</span>
                                    @if(modmailLog.getCloser().mod())
                                        <span class="badge rounded-pill text-bg-primary align-middle fst-normal"
                                              style="font-size: 12px;">
                                            <i class="bi bi-shield"></i>
                                            Moderator
                                        </span>
                                    @endif
                                    <span class="badge rounded-pill text-bg-info align-middle fst-normal"
                                          style="font-size: 12px;">
                                        <i class="bi bi-gear"></i>
                                        System
                                    </span>

                                    <span class="fs-6" data-bs-toggle="tooltip"
                                          title="${modmailLog.getClosedTime().toString()}">${modmailLog.getClosedTime().toString()}</span>
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


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3"
        crossorigin="anonymous"></script>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>