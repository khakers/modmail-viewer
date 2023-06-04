@import com.github.khakers.modmailviewer.Main
@import com.github.khakers.modmailviewer.data.internal.TicketStatus
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param com.github.khakers.modmailviewer.dashboard.DashboardPage page


@template.layout.Page(page = page, content = @`
    <div class="container pt-5">
        <div id="dashboardControlPanel" class="mb-3 p-2 ps-3 border rounded">
            <div class="row justify-content-between">
                <div class="col">
                    <form id="dailyTicketsForm">
                        <div class="row">
                            <div class="col-auto">
                                <label for="chartPeriodSelect" class="col-form-label">Time Period:</label>
                            </div>
                            <div class="col">
                                <select class="form-select" name="dashboardPeriodSelect" id="chartPeriodSelect" aria-label="Time Period Select">
                                    <option value="15">15 days</option>
                                    <option value="30">30 days</option>
                                    <option value="60">60 days</option>
                                    <option selected value="90">90 days</option>
                                </select>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="col">
                    <button type="button" id="dashboardReloadButton" class="btn btn-outline-primary float-end">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-clockwise" viewBox="0 0 16 16">
                            <path fill-rule="evenodd" d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2v1z"/>
                            <path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466z"/>
                        </svg>
                    </button>
                </div>
            </div>
        </div>
        <div class="row row-cols-1 row-cols-md-3 g-3 justify-content-between">
            <div class="col">
                <%-- Total Tickets --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-inbox"></i> Total Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.ALL)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Open Tickets --%>
                <div class="card ">
                    <h6 class="card-header "><i class="bi bi-envelope-paper"></i> Open Tickets</h6>
                    <div class="card-body <%--text-bg-warning--%>">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.OPEN)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Closed Tickets --%>
                <div class="card <%--text-bg-success--%>">
                    <h6 class="card-header"><i class="bi bi-envelope-check"></i> Closed Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.CLOSED)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card <%--text-bg-success--%>">
                    <h6 class="card-header"><i class="bi bi-envelope-exclamation"></i> Unanswered Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.metricsDao.getUnansweredTicketCount()}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-stopwatch"></i> Average Reply Time</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1"><relative-duration duration="${Main.metricsDao.getAverageFirstReplyTime(90).toMillis()}"></relative-duration></h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-stopwatch"></i> Average Resolution Time</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1"><relative-duration duration="${Main.metricsDao.getAverageResolutionTime(90).toMillis()}"></relative-duration></h6>
                    </div>
                </div>
            </div>

        </div>
        <%--    row-cols-1 row-cols-md-2 row-cols-xl-3--%>
        <div class="row pt-3 g-3">
            <div class="col-12 col-lg-5 col-xl-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Ticket Closers</h5>
                        <%--                            <p class="card-text">This is a wider card with supporting text below as a natural lead-in to--%>
                        <%--                                additional content. This content is a little longer.</p>--%>
                        <%--                            <p class="card-text"><small class="text-muted">Last updated 3 mins ago</small></p>--%>
                    </div>
                    <canvas class="chart" id="ticketClosersChart" width="400" height="400" data-chart-data="${Main.metricsDao.getTicketClosersJson()}"></canvas>
                    <%--                        <code class="card-footer">${moderatorCloses.toString()}</code>--%>
                </div>
            </div>
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Tickets closed per day</h5>
                    </div>
                    <canvas class="chart" id="ticketActionsPerDayChart" width="400" data-chart-data="${Main.metricsDao.getTicketsPerDayJson(90)}"></canvas>
                    <%--                        <code class="card-footer"></code>--%>
                </div>
                <%--                    <div class="card">--%>
                <%--                        <div class="card-header">--%>
                <%--                            Recent Messages--%>
                <%--                        </div>--%>
                <%--                        <div class="card-body">--%>
                <%--                            @if(recentEntries == null || recentEntries.isEmpty())--%>
                <%--                                <p class="card-text">No recent entries could be retrieved</p>--%>
                <%--                            @else--%>
                <%--                                <ul class="list-group list-group-flush">--%>
                <%--                                    @for(var entry : recentEntries)--%>
                <%--                                        <li class="list-group-item">--%>
                <%--                                            <a class="fw-bold fs-5" href="/logs/${entry.getKey()}">${entry.getKey()}</a>--%>
                <%--                                            @if(entry.isOpen())--%>
                <%--                                                <span class="badge rounded-pill text-bg-success fs-6 fw-normal">Open</span>--%>
                <%--                                            @else--%>
                <%--                                                <span class="badge rounded-pill text-bg-danger fs-6 fw-semibold"><i--%>
                <%--                                                            class="bi bi-x-circle"></i> Closed</span>--%>
                <%--                                            @endif--%>
                <%--                                            <p>${entry.getMessages().get(entry.getMessages().size()-1).getContent()}</p>--%>
                <%--                                            <span class="text-muted"--%>
                <%--                                                  timestamp="${entry.getMessages().get(entry.getMessages().size()-1).getCreationTime().toString()}"--%>
                <%--                                                  timestamp-type="detailed"></span>--%>
                <%--                                        </li>--%>
                <%--                                    @endfor--%>
                <%--                                </ul>--%>
                <%--                            @endif--%>
                <%--                        </div>--%>
                <%--                    </div>--%>
            </div>
        </div>
    </div>
`)