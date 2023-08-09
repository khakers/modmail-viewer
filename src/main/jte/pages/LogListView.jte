@import com.github.khakers.modmailviewer.data.internal.TicketStatus
@import com.github.khakers.modmailviewer.util.DiscordUtils
@import com.github.khakers.modmailviewer.util.Pagination
@import com.github.khakers.modmailviewer.util.QueryParamUtil
@import com.github.khakers.modmailviewer.util.TextFragmentUtils
@import static com.github.khakers.modmailviewer.jte.JteContext.*




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
        <form class="border ${logEntries.isEmpty() ? "rounded" : "rounded-top"} p-2" id="logSearchForm" action="/"
              method="get" up-submit up-transition="cross-fade" up-target="#logEntryList" up-disable up-history="true">
            <div class="row justify-content-center text-center text-xl-start g-2 align-items-center">
                <div class="col-xl-auto col-sm-12">
                    <div class="btn-group" up-autosubmit up-watch-delay="200" up-form-group>
                        <input type="radio" class="btn-check" name="status" id="all" value="all" autocomplete="off"
                               checked="${ticketStatusFilter.equals(TicketStatus.ALL)}">
                        <label class="btn btn-outline-primary"
                               for="all">${localize("LOG_SEARCH_FORM_STATUS_ALL", modMailLogDB.getTotalTickets(TicketStatus.ALL, search))}</label>

                        <input type="radio" class="btn-check" name="status" id="open" value="open" autocomplete="off"
                               checked="${ticketStatusFilter.equals(TicketStatus.OPEN)}">
                        <label class="btn btn-outline-primary" for="open"><i
                                    class="bi bi-record-circle"></i> ${localize("LOG_SEARCH_FORM_STATUS_OPEN", modMailLogDB.getTotalTickets(TicketStatus.OPEN, search))}
                        </label>

                        <input type="radio" class="btn-check" name="status" id="closed" value="closed"
                               autocomplete="off" checked="${ticketStatusFilter.equals(TicketStatus.CLOSED)}">
                        <label class="btn btn-outline-primary" for="closed"><i
                                    class="bi bi-check-lg"></i> ${localize("LOG_SEARCH_FORM_STATUS_CLOSED", modMailLogDB.getTotalTickets(TicketStatus.CLOSED, search))}
                        </label>
                    </div>
                </div>
                <div class="col-auto mx-1" up-form-group>
                    <label for="itemsPerPage">${localize("LOG_SEARCH_FORM_ITEMS_PER_PAGE_LABEL")}</label>
                    <select class="form-select form-select-sm" name="itemsPerPage" id="itemsPerPage" aria-label=""
                            up-autosubmit up-watch-delay="200">
                        <option value="5" selected="${page.itemsPerPage==5}">5</option>
                        <option value="8" selected="${page.itemsPerPage==8}">8 ${localize("OPTION_DEFAULT")}</option>
                        <option value="15" selected="${page.itemsPerPage==15}">15</option>
                        <option value="25" selected="${page.itemsPerPage==25}">25</option>
                        <option value="50" selected="${page.itemsPerPage==50}">50</option>
                    </select>
                </div>
                <div class="col-xl-auto col-sm-12 pt-2 py-xl-0 d-flex flex-grow-1">
                    <div id="SearchBox" class="input-group px-xl-0 px-sm-2" up-form-group>
                        <button class="btn btn-outline-danger" type="button" id="ClearSearchButton"><i
                                    class="bi bi-x-lg"></i> ${localize("LOG_SEARCH_FORM_SEARCH_RESET")}
                        </button>
                        <input id="SearchBoxInput" name="search" type="search" class="form-control"
                               placeholder="${localize("LOG_SEARCH_FORM_SEARCH_BOX_PLACEHOLDER")}"
                               aria-label="Search Text" aria-describedby="SearchButton" value="${search}">
                        <button class="btn btn-outline-primary" type="submit" id="SearchButton"><i
                                    class="bi bi-search"></i> Search
                        </button>
                    </div>
                </div>
            </div>
        </form>
        @if(logEntries.isEmpty())
            <div class="pt-5">
                <p class="text-center">${localize("LOG_SEARCH_NO_ENTRIES_FOUND")}</p>
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
                                    class="bi bi-record-circle"></i> ${localize("LOG_STATUS_OPEN")}</span>
                    @else
                        <span class="badge rounded-pill text-bg-danger fs-6"><i
                                    class="bi bi-check-circle"></i> ${localize("LOG_STATUS_CLOSED")}</span>
                    @endif
                    @if(entry.get().isNsfw())
                        <span class="badge rounded-pill text-bg-warning fs-6"><i
                                    class="bi bi-exclamation-triangle"></i> ${localize("LOG_NSFW")}</span>
                    @endif
                    @if(entry.get().isOpen())
                        <span>Created by</span>
                        <span class="user-select-all d-inline-flex" data-bs-toggle="tooltip"
                              data-bs-title="${entry.get().getCreator().id()}">
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span>
                                <span class="text-muted">${DiscordUtils.getDiscriminatorString(entry.get().getCreator())}</span>
                            </span>

                        <span><time class="text-muted" datetime="${entry.get().getCreationTime().toString()}"
                                    data-timestamp="${entry.get().getCreationTime().toString()}"
                                    data-timestamp-type="relative" data-bs-toggle="tooltip"
                                    data-timestamp-title-type="detailed"></time></span>
                    @else
                        <span>Created by</span>
                        <span class="user-select-all d-inline-flex" data-bs-toggle="tooltip"
                              data-bs-title="${entry.get().getCreator().id()}">
                            <span class="fw-semibold">${entry.get().getCreator().name()}</span>
                                <span class="text-muted">${DiscordUtils.getDiscriminatorString(entry.get().getCreator())}</span>
                            </span>
                        <span>was closed <time class="text-muted"
                                               datetime="${entry.get().getClosedTime().get().toString()}"
                                               data-timestamp="${entry.get().getClosedTime().get().toString()}"
                                               data-timestamp-type="relative" data-bs-toggle="tooltip"
                                               data-timestamp-title-type="detailed"></time></span>
                        @if(entry.get().getCloser().isPresent())
                            <span>by</span>
                            <span class="user-select-all d-inline-flex" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.get().getCloser().get().id()}">
                                <span class="fw-semibold">${entry.get().getCloser().get().name()}</span>
                                    <span class="text-muted">${DiscordUtils.getDiscriminatorString(entry.get().getCloser().get())}</span>
                                </span>
                        @endif

                    @endif
                    ·
                    <span>${entry.get().getMessages().size()} messages</span>
                    @if(!entry.get().getMessages().isEmpty())
                        ·
                        <span>Last message sent <time class="text-muted"
                                                      datetime="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getCreationTime().toString()}"
                                                      data-timestamp="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getCreationTime().toString()}"
                                                      data-timestamp-type="relative" data-bs-toggle="tooltip"
                                                      data-timestamp-title-type="detailed"></time> by
                            <span class="user-select-all d-inline-flex" data-bs-toggle="tooltip"
                                  data-bs-title="${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().id()}">
                                <span class="fw-semibold">${entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor().name()}</span>
                                <span class="text-muted">${DiscordUtils.getDiscriminatorString(entry.get().getMessages().get(entry.get().getMessages().size()-1).getAuthor())}</span>
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
        <div class="container col d-flex justify-content-center p-4 pb-0" up-hungry>
            <nav aria-label="pagination">
                <ul class="pagination">
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