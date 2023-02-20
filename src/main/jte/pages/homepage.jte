@import com.github.khakers.modmailviewer.auth.SiteUser
@import com.github.khakers.modmailviewer.data.internal.TicketStatus
@import com.github.khakers.modmailviewer.util.Pagination
@import com.github.khakers.modmailviewer.ModMailLogDB
@import com.github.khakers.modmailviewer.util.QueryParamUtil
@import com.github.khakers.modmailviewer.util.TextFragmentUtils
@import io.javalin.http.Context


@param Context ctx


@param java.util.List<com.github.khakers.modmailviewer.data.ModMailLogEntry> logEntries

@param int page
@param int pageCount
@param SiteUser user
@param ModMailLogDB modMailLogDB
@param TicketStatus ticketStatusFilter
@param boolean showNSFW
@param String search

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail Logs</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    @template.macros.HeaderImports()
</head>
<body>

<div class="page-container">
    @template.macros.navbar(ctx = ctx, user = user)
    <div class="content">
        <div class="container pt-5">
            <div class="border ${logEntries.isEmpty() ? "rounded" : "rounded-top"} p-2">
                <div class="row justify-content-center justify-content-lg-end">
                    <div class="col-xl-3 col-sm-12">
                        <ul class="nav nav-pills ">
                            <li class="nav-item">
                                <a class="nav-link @if(ticketStatusFilter == TicketStatus.ALL)active@endif"
                                   href="${QueryParamUtil.updateQueryParams(ctx, "status", "all")}"
                                >${modMailLogDB.getTotalTickets(TicketStatus.ALL, search)} All</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link @if(ticketStatusFilter == TicketStatus.OPEN)active@endif"
                                   href="${QueryParamUtil.updateQueryParams(ctx, "status", "open")}"><i
                                            class="bi bi-record-circle"></i> ${modMailLogDB.getTotalTickets(TicketStatus.OPEN, search)}
                                    Open</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link @if(ticketStatusFilter == TicketStatus.CLOSED)active@endif"
                                   href="${QueryParamUtil.updateQueryParams(ctx, "status", "closed")}"><i
                                            class="bi bi-check-lg"></i> ${modMailLogDB.getTotalTickets(TicketStatus.CLOSED, search)}
                                    Closed</a>
                            </li>
                            <%--                    <li class="nav-item border-danger btn-outline-danger ps-2">--%>
                            <%--                        <a class="nav-link border-danger @if(showNSFW)active@endif" href="#"--%>
                            <%--                           onclick="update('nsfw', '${!showNSFW}')">Show NSFW</a>--%>
                            <%--                    </li>--%>
                            <%--                    <li class="nav-item ps-5 ms-auto">--%>
                            <%--                        <button type="button" class="btn btn-outline-danger" onclick="update('nsfw', '${!showNSFW}')">Show NSFW</button>--%>
                            <%--                        --%><%-- THis painful if is only required because of 'checked'. JTE does not allow expressions for attribute names --%>
                            <%--                        @if(showNSFW)--%>
                            <%--                            <input type="checkbox" class="btn-check" id="btncheck1" autocomplete="off" onclick="update('nsfw', '${!showNSFW}')" checked>--%>
                            <%--                        @else--%>
                            <%--                            <input type="checkbox" class="btn-check" id="btncheck1" autocomplete="off" onclick="update('nsfw', '${!showNSFW}')">--%>
                            <%--                        @endif--%>
                            <%--                        <label class="btn btn-outline-danger" for="btncheck1">Show NSFW</label>--%>
                            <%--                    </li>--%>
                        </ul>
                    </div>
                    <div class="col-xl-9 col-sm-12 pt-2 py-xl-0">
                        <div id="SearchBox" class="input-group px-xl-0 px-sm-2">
                            <button class="btn btn-outline-danger" type="button" id="ClearSearchButton"><i class="bi bi-x-lg"></i> Clear</button>
                            <input id="SearchBoxInput" type="text" class="form-control" placeholder="Search"
                                   aria-label="Search Text" aria-describedby="SearchButton">
                            <button class="btn btn-outline-primary" type="button" id="SearchButton"> <i class="bi bi-search"></i> Search</button>
                        </div>
                    </div>
                </div>
            </div>
            @if(logEntries.isEmpty())
                <div class="pt-5">
                    <p class="text-center">Looks like you don't have any
                        modmail threads matching these filters</p>
                    <p class="text-center text-muted">¯\_(ツ)_/¯</p>
                </div>
            @endif
            <ol class="list-group">
                @for(var entry : gg.jte.support.ForSupport.of(logEntries))
                    <li class="list-group-item @if(entry.isFirst())border-top-0 rounded-0@endif">
                        <h2><a href="/logs/${entry.get().getKey()}${TextFragmentUtils.getTextFragmentAsSnippet(search)}">${entry.get().getKey()}</a></h2>
                        @if(entry.get().isOpen())
                            <span class="badge rounded-pill text-bg-success fs-6"><i
                                        class="bi bi-record-circle"></i> Open</span>
                        @else
                            <span class="badge rounded-pill text-bg-danger fs-6"><i
                                        class="bi bi-check-circle"></i> Closed</span>
                        @endif
                        @if(entry.get().isNsfw())
                            <span class="badge rounded-pill text-bg-warning fs-6"><i
                                        class="bi bi-exclamation-triangle"></i> NSFW</span>
                        @endif
                        @if(entry.get().isOpen())
                            <span>Created by</span>
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span><span
                                class="text-muted">#${entry.get().getCreator().discriminator()}</span>
                            <span><span class="text-muted" data-timestamp="${entry.get().getCreationTime().toString()}"
                                        data-timestamp-type="relative" data-bs-toggle="tooltip"
                                        data-timestamp-title-type="detailed"></span></span>
                        @else
                            <span>Created by</span>
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span><span
                                class="text-muted">#${entry.get().getCreator().discriminator()}</span>
                            <span>was closed <span class="text-muted"
                                                   data-timestamp="${entry.get().getClosedTime().get().toString()}"
                                                   data-timestamp-type="relative" data-bs-toggle="tooltip"
                                                   data-timestamp-title-type="detailed"></span></span>

                        @endif
                        ·
                        <span>${entry.get().getMessages().size()} messages</span>
                        ·
                        <span>Last message sent <span class="text-muted"
                                                      data-timestamp="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getCreationTime().toString()}"
                                                      data-timestamp-type="relative" data-bs-toggle="tooltip"
                                                      data-timestamp-title-type="detailed"></span> by
                            <span class="user-select-all" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().id()}"><span
                                        class="fw-semibold">${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().name()}</span><span
                                        class="text-muted">#${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().discriminator()}</span>
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
                <nav aria-label="Page navigation">
                    <ul class="pagination">

                        <%--                        onclick="update('page', '${Math.max(page-1, 1)}')"--%>
                        <li class="page-item"><a class="page-link ${previousClass}"
                                                 href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(Math.max(page-1, 1)))}">Previous</a>
                        </li>
                        @if(Pagination.needsPaginationCulling(pageCount) && page > 4)
                            <a class="page-link" href="#">1</a>
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
                                        <a class="page-link"
                                           href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(i))}">${i}</a>
                                    @endif
                                </li>
                            @endif
                        @endfor
                        @if(Pagination.needsPaginationCulling(pageCount) && page < pageCount-3)
                            <li class="page-item"><a class="page-link disabled">...</a></li>
                            <a class="page-link sm-visually-hidden" href="#">${pageCount}</a>
                        @endif
                        <li class="page-item"><a class="page-link ${nextClass}"
                                                 href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(page+1))}"
                            >Next</a></li>
                    </ul>
                </nav>
            </div>
        @endif
    </div>
    @template.macros.footer()
</div>

@template.macros.JSImports()

</body>
</html>