@import com.github.khakers.modmailviewer.auth.SiteUser
@import com.github.khakers.modmailviewer.util.Pagination
@import com.github.khakers.modmailviewer.data.ModMailLogEntry

@param java.util.List<com.github.khakers.modmailviewer.data.ModMailLogEntry> logEntries

@param int page
@param int pageCount
@param SiteUser user

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Logs</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <%--    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet"--%>
    <%--          integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">--%>
    <%--    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.9.1/font/bootstrap-icons.css"--%>
    <%--          integrity="sha384-xeJqLiuOvjUBq3iGOjvSQSIlwrpqjSHXpduPd6rQpuiM3f5/ijby8pCsnbu5S81n"--%>
    <%--          crossorigin="anonymous">--%>
    <link href="/webjars/bootstrap/5.2.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/webjars/bootstrap-icons/1.10.2/font/bootstrap-icons.css">
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<div class="page-container">
    @template.macros.navbar(user = user)
    <div class="content">
        <div class="container pt-5">
            @if(logEntries.isEmpty())
                <p class="text-center">Looks like you don't have any modmail threads</p>
                <p class="text-center text-muted">¯\_(ツ)_/¯</p>
            @endif
            <ol class="list-group">
                @for(ModMailLogEntry entry : logEntries)
                    <li class="list-group-item">
                        <h2><a href="/logs/${entry.getKey()}">${entry.getKey()}</a></h2>
                        @if(entry.isOpen())
                            <span class="badge rounded-pill text-bg-success fs-6"><i
                                        class="bi bi-record-circle"></i> Open</span>
                        @else
                            <span class="badge rounded-pill text-bg-danger fs-6"><i
                                        class="bi bi-check-circle"></i> Closed</span>
                        @endif
                        @if(entry.isNsfw())
                            <span class="badge rounded-pill text-bg-warning fs-6"><i
                                        class="bi bi-exclamation-triangle"></i> NSFW</span>
                        @endif
                        <span>Created by:</span>
                        <span class="fw-semibold">${entry.getCreator().name()}</span><span
                                class="text-secondary">#${entry.getCreator().discriminator()}</span>
                        <span>on <span class="text-muted" timestamp="${entry.getCreationTime().toString()}"
                                       timestamp-type="detailed"></span></span>
                        ·
                        <span>${entry.getMessages().size()} messages</span>
                        ·
                        <span>Last message sent <span class="text-muted"
                                                      timestamp="${entry.getMessages().get(entry.getMessages().size()-1).getCreationTime().toString()}"
                                                      timestamp-type="relative" data-bs-toggle="tooltip"
                                                      timestamp-title-type="detailed"></span> by
                            <span class="user-select-all" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.getMessages().get(entry.getMessages().size()-1).getAuthor().id()}"><span
                                        class="fw-semibold">${entry.getMessages().get(entry.getMessages().size()-1).getAuthor().name()}</span><span
                                        class="text-secondary">#${entry.getMessages().get(entry.getMessages().size()-1).getAuthor().discriminator()}</span>
                            </span>
                        </span>
                    </li>
                @endfor

            </ol>
        </div>
        <%-- todo Fix mobile pagination --%>
        !{String previousClass = null;}
        @if(page==1)
            !{previousClass="disabled";}
        @endif
        !{String nextClass = null;}
        @if(page==pageCount)
            !{nextClass="disabled";}
        @endif
        @if(pageCount > 1)
            <div class="container col d-flex justify-content-center p-4">
                <nav aria-label="Page navigation example">
                    <ul class="pagination">
                        <li class="page-item"><a class="page-link ${previousClass}"
                                                 href="?page=${Math.max(page-1, 1)}">Previous</a>
                        </li>
                        @if(Pagination.needsPaginationCulling(pageCount) && page > 4)
                            <a class="page-link" href="?page=1">1</a>
                            <li class="page-item"><a class="page-link disabled">...</a></li>
                        @endif
                        @for(int i = 1; i < pageCount+1; ++i)
                            @if(Pagination.shouldDisplayPage(i, page, pageCount))
                                !{String isActive = null;}
                                @if(i == page)
                                    !{isActive="active";}
                                @endif
                                <li class="page-item">
                                    @if(i == page)
                                        <span class="page-link ${isActive}">${i}</span>
                                    @else
                                        <a class="page-link" href="?page=${i}">${i}</a>
                                    @endif
                                </li>
                            @endif
                        @endfor
                        @if(Pagination.needsPaginationCulling(pageCount) && page < pageCount-3)
                            <li class="page-item"><a class="page-link disabled">...</a></li>
                            <a class="page-link sm-visually-hidden" href="?page=${pageCount}">${pageCount}</a>
                        @endif
                        <li class="page-item"><a class="page-link ${nextClass}" href="?page=${page+1}">Next</a></li>
                    </ul>
                </nav>
            </div>
        @endif
    </div>
    @template.macros.footer()
</div>

<script src="timeformat.js"></script>
<script src="/webjars/bootstrap/5.2.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>