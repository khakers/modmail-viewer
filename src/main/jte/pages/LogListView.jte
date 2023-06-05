@import com.github.khakers.modmailviewer.data.internal.TicketStatus
@import com.github.khakers.modmailviewer.util.DiscordUtils
@import com.github.khakers.modmailviewer.util.Pagination
@import com.github.khakers.modmailviewer.util.QueryParamUtil
@import com.github.khakers.modmailviewer.util.TextFragmentUtils



@param com.github.khakers.modmailviewer.log.LogsPage page


!{var logEntries = page.logEntries;}
!{var pageIndex = page.currentPage;}
!{var pageCount = page.pageCount;}
!{var ticketStatusFilter = page.ticketStatusFilter;}
!{var search = page.searchString;}
!{var ctx = page.getCtx();}
!{var modMailLogDB = page.modMailLogDB;}

@template.layout.Page(page = page, content = @`
    <div class="container pt-5">
        <div class="border ${logEntries.isEmpty() ? "rounded" : "rounded-top"} p-2">
            <div class="row justify-content-center justify-content-lg-end">
                <div class="col-xl-4 col-sm-12">
                    <ul class="nav nav-pills ">
                        <li class="nav-item">
                            <a class="nav-link @if(ticketStatusFilter == TicketStatus.ALL || ticketStatusFilter == null)active@endif"
                               up-follow up-target=".content"
                               up-transition="cross-fade"
                               up-alias="/"
                               href="${QueryParamUtil.updateQueryParams(ctx, "status", "all")}"
                            >${modMailLogDB.getTotalTickets(TicketStatus.ALL, search)} All</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link @if(ticketStatusFilter == TicketStatus.OPEN)active@endif"
                               up-follow up-target=".content"
                               up-transition="cross-fade"
                               href="${QueryParamUtil.updateQueryParams(ctx, "status", "open")}"><i
                                        class="bi bi-record-circle"></i> ${modMailLogDB.getTotalTickets(TicketStatus.OPEN, search)}
                                Open</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link @if(ticketStatusFilter == TicketStatus.CLOSED)active@endif"
                               up-target=".content" up-follow
                               up-transition="cross-fade"
                               href="${QueryParamUtil.updateQueryParams(ctx, "status", "closed")}"><i
                                        class="bi bi-check-lg"></i> ${modMailLogDB.getTotalTickets(TicketStatus.CLOSED, search)}
                                Closed</a>
                        </li>
                    </ul>
                </div>
                <div class="col-xl-8 col-sm-12 pt-2 py-xl-0">
                    <div id="SearchBox" class="input-group px-xl-0 px-sm-2">
                        <button class="btn btn-outline-danger" type="button" id="ClearSearchButton"><i
                                    class="bi bi-x-lg"></i> Reset
                        </button>
                        <input id="SearchBoxInput" type="search" class="form-control"
                               placeholder="Search Modmail logs..."
                               aria-label="Search Text" aria-describedby="SearchButton" value="${search}">
                        <button class="btn btn-outline-primary" type="button" id="SearchButton"><i
                                    class="bi bi-search"></i> Search
                        </button>
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
        <ol id="logEntryList" class="list-group">
            @for(var entry : gg.jte.support.ForSupport.of(logEntries))
                <li class="list-group-item @if(entry.isFirst())border-top-0 rounded-0@endif"
                    id="${entry.get().get_id()}">
                    <h2>
                        <a href="/logs/${entry.get().getKey()}${TextFragmentUtils.getTextFragmentAsSnippet(search)}"
                           up-follow up-target=".content" up-transition="move-left">${entry.get().getKey()}</a>
                    </h2>
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
                        <span class="user-select-all" data-bs-toggle="tooltip"
                              data-bs-title="${entry.get().getCreator().id()}">
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span>
                            @if(!DiscordUtils.isMigratedUserName(entry.get().getCreator()))
                                <span class="text-muted">#${entry.get().getCreator().discriminator()}</span>
                            @endif
                            </span>

                        <span><span class="text-muted" data-timestamp="${entry.get().getCreationTime().toString()}"
                                    data-timestamp-type="relative" data-bs-toggle="tooltip"
                                    data-timestamp-title-type="detailed"></span></span>
                    @else
                        <span>Created by</span>
                        <span class="user-select-all" data-bs-toggle="tooltip"
                              data-bs-title="${entry.get().getCreator().id()}">
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span>
                            @if(!DiscordUtils.isMigratedUserName(entry.get().getCreator()))
                                <span class="text-muted">#${entry.get().getCreator().discriminator()}</span>
                            @endif
                            </span>
                        <span>was closed <span class="text-muted"
                                               data-timestamp="${entry.get().getClosedTime().get().toString()}"
                                               data-timestamp-type="relative" data-bs-toggle="tooltip"
                                               data-timestamp-title-type="detailed"></span></span>
                        @if(entry.get().getCloser().isPresent())
                            <span>by</span>
                            <span class="user-select-all" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.get().getCloser().get().id()}">
                                <span class="fw-semibold">${entry.get().getCloser().get().name()}</span>
                                @if(!DiscordUtils.isMigratedUserName(entry.get().getCreator()))
                                    <span class="text-muted">#${entry.get().getCloser().get().discriminator()}</span>
                                @endif
                                </span>
                        @endif

                    @endif
                    ·
                    <span>${entry.get().getMessages().size()} messages</span>
                    @if(!entry.get().getMessages().isEmpty())
                        ·
                        <span>Last message sent <span class="text-muted"
                                                      data-timestamp="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getCreationTime().toString()}"
                                                      data-timestamp-type="relative" data-bs-toggle="tooltip"
                                                      data-timestamp-title-type="detailed"></span> by
                            <span class="user-select-all" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().id()}">
                                <span class="fw-semibold">${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().name()}</span>
                            @if(!DiscordUtils.isMigratedUserName(entry.get().getCreator()))
                                <span class="text-muted">#${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().discriminator()}</span>
                            @endif
                            </span>
                        </span>
                    @endif

                </li>
            @endfor

        </ol>
    </div>
    <%-- todo Fix mobile pagination --%>
    !{String previousClass = null;}
    @if(pageIndex==1)
        !{previousClass="disabled";}
    @endif
    !{String nextClass = null;}
    @if(pageIndex==pageCount)
        !{nextClass="disabled";}
    @endif
    @if(pageCount > 1)
        <div class="container col d-flex justify-content-center p-4">
            <nav aria-label="pagination">
                <ul class="pagination">
                    <%--                        onclick="update('pageIndex', '${Math.max(pageIndex-1, 1)}')"--%>
                    <li class="page-item">
                        <a class="page-link ${previousClass}" up-follow="#logEntryList"
                           href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(Math.max(pageIndex-1, 1)))}">Previous</a>
                    </li>
                    @if(Pagination.needsPaginationCulling(pageCount) && pageIndex > 4)
                        <a class="page-link"
                           href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(1))}">1</a>
                        <li class="page-item"><a class="page-link disabled">...</a></li>
                    @endif
                    @for(int i = 1; i < pageCount+1; ++i)
                        @if(Pagination.shouldDisplayPage(i, pageIndex, pageCount))
                            !{String isActive = null;}
                            @if(i == pageIndex)
                                !{isActive="active";}
                            @endif
                            <li class="page-item">
                                @if(i == pageIndex)
                                    <span class="page-link ${isActive}">${i}</span>
                                @else
                                    <a class="page-link"
                                       up-follow="#logEntryList"
                                       href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(i))}">${i}</a>
                                @endif
                            </li>
                        @endif
                    @endfor
                    @if(Pagination.needsPaginationCulling(pageCount) && pageIndex < pageCount-3)
                        <li class="page-item"><a class="page-link disabled">...</a></li>
                        <a class="page-link sm-visually-hidden"
                           href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(pageCount))}">${pageCount}</a>
                    @endif
                    <li class="page-item"><a class="page-link ${nextClass}" up-follow="#logEntryList"
                                             href="${QueryParamUtil.updateQueryParams(ctx, "page", String.valueOf(pageIndex+1))}"
                        >Next</a></li>
                </ul>
            </nav>
        </div>
    @endif
`)